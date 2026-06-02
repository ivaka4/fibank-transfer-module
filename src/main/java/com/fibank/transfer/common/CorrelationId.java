package com.fibank.transfer.common;

/** Constants for propagating a correlation id across the request and into the logs. */
public final class CorrelationId {

    /** Inbound/outbound HTTP header carrying the correlation id. */
    public static final String HEADER = "X-Correlation-Id";

    /** Key under which the correlation id is stored in the logging MDC. */
    public static final String MDC_KEY = "correlationId";

    private CorrelationId() {
    }
}
