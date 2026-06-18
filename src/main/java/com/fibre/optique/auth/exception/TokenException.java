package com.fibre.optique.auth.exception;

/**
 * Thrown when a JWT or refresh token is invalid, expired, or revoked.
 * Maps to HTTP 401 via {@code GlobalExceptionHandler}.
 */
public class TokenException extends RuntimeException {

    public TokenException(String message) {
        super(message);
    }
}
