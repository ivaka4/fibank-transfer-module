package com.fibank.transfer.dto.response;

import java.time.LocalDateTime;

/**
 * Consistent error envelope returned by the global exception handler for every
 * failed request, matching the structure required by the assignment.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
