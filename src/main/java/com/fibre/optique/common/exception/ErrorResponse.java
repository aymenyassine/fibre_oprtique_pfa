package com.fibre.optique.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response body returned by {@link GlobalExceptionHandler}.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** HTTP status code */
    private int status;

    /** Short error category (e.g. "NOT_FOUND", "VALIDATION_ERROR") */
    private String error;

    /** Human-readable message */
    private String message;

    /** Request path that triggered the error */
    private String path;

    /** Timestamp of the error */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /** Field-level validation errors — only present on 400 responses */
    private List<FieldError> fieldErrors;

    @Data
    @Builder
    public static class FieldError {
        private String field;
        private String rejectedValue;
        private String message;
    }
}
