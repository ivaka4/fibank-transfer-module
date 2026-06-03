# FiBank — Inter-Account Transfer & Standing Order Module

A backend module that manages internal fund transfers between bank accounts, including
one-time transfers and recurring standing orders. It guarantees transactional integrity
through double-entry bookkeeping, handles concurrent access, supports cross-currency
transfers with a configurable FX rate, executes scheduled standing orders automatically,
and makes every operation idempotent and fully auditable.

---

## Tech stack

| Concern        | Choice                                                        |
|----------------|--------------------------------------------------------------|
| Language       | Java 25                                                      |
| Framework      | Spring Boot 4 (Spring Framework 7, Jakarta EE 11)            |
| Build          | Maven (Maven Wrapper included — no local Maven required)    |
| Database       | H2 in-memory                                                 |
| Schema         | Liquibase (versioned changesets)                             |
| Mapping        | MapStruct                                                    |
| Logging        | Log4j2 with asynchronous loggers (LMAX Disruptor)           |
| Scheduling     | Spring `@Scheduled` + ShedLock (distributed lock)           |
| Security       | Spring Security (stateless) + custom API-key filter         |
| Concurrency    | JDBC virtual threads; pessimistic locking on transfers      |
| Tests          | JUnit 5, Mockito, Spring Boot Test (MockMvc)                |

---

## Architecture

Classic **layered architecture** with one-directional dependencies:

```
controller  ->  service  ->  repository  ->  entity
   (web)      (business)    (Spring Data)    (JPA)
```

- **Controllers** are thin: they map DTOs and delegate to services.
- **Services** hold all business logic, each behind an interface (DIP / testability).
- **Repositories** are Spring Data JPA interfaces.
- **DTOs** (`record`s with Bean Validation) isolate the API contract from entities; mapping is done by **MapStruct**.

### Key design decisions

- **Double-entry bookkeeping** — every successful transfer creates exactly two append-only
  ledger entries (DEBIT source, CREDIT destination), produced by a single `LedgerEntryFactory`
  so the invariant lives in one place. The ledger is never updated or deleted.
- **Concurrency** — `SELECT … FOR UPDATE` (pessimistic write-lock) on the involved accounts,
  acquired in a deterministic IBAN order to make deadlocks impossible; an `@Version` column adds
  optimistic locking as defence-in-depth. The whole transfer runs in one transaction, so it is
  fully atomic.
- **Idempotency** — each transfer requires a client `X-Idempotency-Key`. The first call is
  executed and its result stored (with a 24h TTL); a replay of the same key returns the original
  result without re-executing. Reusing a key with a different payload is rejected (409).
- **FX conversion** — applied through a `CurrencyConverter` *Strategy*. The default reads a
  configurable rate (`fx.rate.usd-to-eur`, default `0.86`); USD→EUR uses it directly, EUR→USD its
  reciprocal. The interface makes swapping in a live-rate provider trivial (Open/Closed).
- **Validation chain** — transfer rules (positive amount, distinct accounts, sufficient funds,
  daily limit) are independent validators executed as a *Chain of Responsibility*, so a new rule
  is added without touching the existing ones.
- **Standing orders** — a single ShedLock-guarded cron job scans active orders and runs those due
  (per-order cron evaluated via Spring `CronExpression`). Each order runs in its own transaction
  and its run status is recorded in a separate one, so a failed order is logged, stays active and
  is retried on the next run — never skipped silently.
- **Global error handling** — `@RestControllerAdvice` maps every exception to a consistent
  `ErrorResponse` envelope (`timestamp, status, error, message, path`), including a custom 401 for
  authentication failures so all responses share one contract.
- **Observability** — Log4j2 async logging with a correlation id (`X-Correlation-Id`) propagated
  via MDC; every transfer attempt is logged (success and failure) with that id.

### Design patterns used
Layered architecture · Strategy (FX) · Chain of Responsibility (validation) · Factory
(ledger pair) · Specification (ledger filtering) · Repository · DTO + Mapper · Value Object (`Money`).

---

## How to run

### With Docker (recommended)

```bash
docker compose up --build
```

The app starts on **http://localhost:8080**. H2 runs in-memory inside the container; Liquibase
creates the schema and seeds the four demo accounts on startup.

Configuration is provided via environment variables (with sensible defaults in `docker-compose.yml`):

| Variable               | Default                     | Purpose                         |
|------------------------|-----------------------------|---------------------------------|
| `FIB_API_KEY`          | `supersecret-fib-api-key`   | Value expected in `X-FIB-AUTH`  |
| `FX_RATE_USD_TO_EUR`   | `0.86`                      | USD→EUR exchange rate           |
| `STANDING_ORDER_CRON`  | `0 * * * * *` (every minute)| Standing-order job schedule     |

Override any of them, e.g.:

```bash
FIB_API_KEY=my-secret docker compose up --build
```

### Locally (requires JDK 25)

```bash
./mvnw verify        # compile + run all tests
./mvnw spring-boot:run
```

---

## Authentication

Every endpoint requires the API key in a custom header:

```
X-FIB-AUTH: supersecret-fib-api-key
```

Missing or invalid keys return `401 Unauthorized` in the standard error format. The key is read
from configuration (env var / profile), never hardcoded.

---

## API

| Method | Endpoint                       | Description                              |
|--------|--------------------------------|------------------------------------------|
| POST   | `/api/v1/transfers`            | Execute a one-time transfer              |
| GET    | `/api/v1/transfers/{id}`       | Get transfer details                     |
| GET    | `/api/v1/ledger`               | Query ledger (paginated, filtered)       |
| GET    | `/api/v1/accounts`             | List accounts with balances              |
| GET    | `/api/v1/accounts/{iban}`      | Get a single account                     |
| POST   | `/api/v1/standing-orders`      | Create a standing order                  |
| GET    | `/api/v1/standing-orders`      | List active standing orders              |
| GET    | `/api/v1/standing-orders/{id}` | Get a standing order                     |
| DELETE | `/api/v1/standing-orders/{id}` | Cancel (soft-delete) a standing order    |

### Example — execute a transfer

```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "X-FIB-AUTH: supersecret-fib-api-key" \
  -H "X-Idempotency-Key: $(uuidgen)" \
  -H "Content-Type: application/json" \
  -d '{"sourceIban":"BG01FINV001","destinationIban":"BG01FINV002","amount":1000.00}'
```

### Ledger query parameters
`accountIban`, `dateFrom`, `dateTo` (ISO-8601), `type` (`DEBIT`/`CREDIT`), `minAmount`, `maxAmount`,
`page`, `size` (default 20). All optional, combined with AND.

---

## Seeded accounts

| IBAN         | Owner          | Currency | Initial balance |
|--------------|----------------|----------|----------------:|
| BG01FINV001  | Ivan Petrov    | USD      |       10 000.00 |
| BG01FINV002  | Maria Koleva   | EUR      |        5 000.00 |
| BG01FINV003  | Georgi Ivanov  | USD      |        2 500.00 |
| BG01FINV004  | Elena Todorova | EUR      |        8 000.00 |

---

## Testing

```bash
./mvnw verify
```

- **Unit tests** (`TransferServiceImplTest`) cover the required scenarios: same-currency transfer,
  cross-currency transfer with FX, insufficient funds, daily-limit exceeded, idempotent replay.
- **FX converter tests** cover USD↔EUR conversion and the same-currency no-op.
- **Integration test** (`TransferIntegrationTest`, `@SpringBootTest` + MockMvc) exercises the full
  transfer flow end-to-end through the security filter, controller, service and persistence.
- **Concurrency test** (`ConcurrentTransferTest`) fires many simultaneous transfers from one account
  and asserts money conservation (no lost updates) under real contention.

---

## Postman

`postman/` contains a collection and environment covering all endpoints (plus idempotency,
daily-limit and unauthorized scenarios). Import both, select the *FiBank — Local* environment and run.
