package com.fibre.optique.auth.exception;

/**
 * Thrown when authentication fails (bad credentials, expired token, etc.)
 * Maps to HTTP 401 via {@code GlobalExceptionHandler}.
 */
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
