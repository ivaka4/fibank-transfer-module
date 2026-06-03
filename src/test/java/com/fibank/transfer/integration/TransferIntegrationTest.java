package com.fibank.transfer.integration;

import com.fibank.transfer.dto.response.AccountResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test of the full transfer flow through the HTTP layer (security filter,
 * controller, service, persistence, Liquibase-seeded H2). MockMvc is assembled from
 * the web application context with the Spring Security filter chain applied.
 */
@SpringBootTest(properties = "fib.api-key=test-api-key")
class TransferIntegrationTest {

    private static final String API_KEY_HEADER = "X-FIB-AUTH";
    private static final String API_KEY = "test-api-key";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void executesTransferEndToEndAndUpdatesBalancesAndLedger() throws Exception {
        String body = """
                {"sourceIban":"BG01FINV001","destinationIban":"BG01FINV003","amount":1000.00}
                """;

        mockMvc.perform(post("/api/v1/transfers")
                        .header(API_KEY_HEADER, API_KEY)
                        .header("X-Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.sourceCurrency").value("USD"))
                .andExpect(jsonPath("$.destinationCurrency").value("USD"));

        assertThat(fetchAccount("BG01FINV001").balance()).isEqualByComparingTo("9000.00");
        assertThat(fetchAccount("BG01FINV003").balance()).isEqualByComparingTo("3500.00");

        mockMvc.perform(get("/api/v1/ledger")
                        .header(API_KEY_HEADER, API_KEY)
                        .param("accountIban", "BG01FINV001")
                        .param("type", "DEBIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].amount").value(1000.00))
                .andExpect(jsonPath("$.content[0].type").value("DEBIT"));
    }

    @Test
    void rejectsRequestWithoutApiKey() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    private AccountResponse fetchAccount(String iban) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/accounts/" + iban)
                        .header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), AccountResponse.class);
    }
}
