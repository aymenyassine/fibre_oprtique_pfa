package com.fibre.optique.common.exception;

import com.fibre.optique.auth.exception.AuthException;
import com.fibre.optique.auth.exception.TokenException;
import com.fibre.optique.billing.exception.BillingValidationException;
import com.fibre.optique.billing.exception.FactureNotFoundException;
import com.fibre.optique.network.exception.NetworkResourceNotFoundException;
import com.fibre.optique.network.exception.NetworkValidationException;
import com.fibre.optique.offer.exception.OfferNotFoundException;
import com.fibre.optique.offer.exception.OfferValidationException;
import com.fibre.optique.request.exception.DemandeNotFoundException;
import com.fibre.optique.request.exception.DemandeValidationException;
import com.fibre.optique.subscription.exception.ContratNotFoundException;
import com.fibre.optique.subscription.exception.SubscriptionNotFoundException;
import com.fibre.optique.subscription.exception.SubscriptionValidationException;
import com.fibre.optique.support.exception.TicketNotFoundException;
import com.fibre.optique.support.exception.TicketValidationException;
import com.fibre.optique.users.exception.UserNotFoundException;
import com.fibre.optique.users.exception.UserValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Centralised exception handler — translates domain exceptions into consistent
 * HTTP error responses using {@link ErrorResponse}.
 *
 * <p>Every module's specific exceptions are handled here so controllers stay clean.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // =========================================================================
    // 404 — Resource Not Found
    // =========================================================================

    @ExceptionHandler({
            UserNotFoundException.class,
            OfferNotFoundException.class,
            SubscriptionNotFoundException.class,
            ContratNotFoundException.class,
            FactureNotFoundException.class,
            TicketNotFoundException.class,
            DemandeNotFoundException.class,
            NetworkResourceNotFoundException.class
    })
    ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        log.debug("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("NOT_FOUND")
                        .message(ex.getMessage())
                        .path(req.getRequestURI())
                        .build());
    }

    // =========================================================================
    // 400 — Domain Validation
    // =========================================================================

    @ExceptionHandler({
            UserValidationException.class,
            OfferValidationException.class,
            SubscriptionValidationException.class,
            BillingValidationException.class,
            TicketValidationException.class,
            DemandeValidationException.class,
            NetworkValidationException.class
    })
    ResponseEntity<ErrorResponse> handleDomainValidation(RuntimeException ex, HttpServletRequest req) {
        log.debug("Domain validation error: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("VALIDATION_ERROR")
                        .message(ex.getMessage())
                        .path(req.getRequestURI())
                        .build());
    }

    // =========================================================================
    // 400 — Bean Validation (@Valid / @Validated)
    // =========================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> ErrorResponse.FieldError.builder()
                        .field(fe.getField())
                        .rejectedValue(fe.getRejectedValue() != null
                                ? fe.getRejectedValue().toString() : null)
                        .message(fe.getDefaultMessage())
                        .build())
                .toList();

        log.debug("Bean validation failed: {} field error(s)", fieldErrors.size());

        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("VALIDATION_ERROR")
                        .message("La requête contient des champs invalides.")
                        .path(req.getRequestURI())
                        .fieldErrors(fieldErrors)
                        .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest req) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(cv -> ErrorResponse.FieldError.builder()
                        .field(extractField(cv))
                        .rejectedValue(cv.getInvalidValue() != null
                                ? cv.getInvalidValue().toString() : null)
                        .message(cv.getMessage())
                        .build())
                .toList();

        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("VALIDATION_ERROR")
                        .message("Violation de contrainte détectée.")
                        .path(req.getRequestURI())
                        .fieldErrors(fieldErrors)
                        .build());
    }

    // =========================================================================
    // 400 — Malformed request
    // =========================================================================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("MALFORMED_REQUEST")
                        .message("Le corps de la requête est malformé ou manquant.")
                        .path(req.getRequestURI())
                        .build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("MISSING_PARAMETER")
                        .message("Paramètre obligatoire manquant : " + ex.getParameterName())
                        .path(req.getRequestURI())
                        .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("TYPE_MISMATCH")
                        .message("Valeur invalide pour le paramètre '%s' : '%s'"
                                .formatted(ex.getName(), ex.getValue()))
                        .path(req.getRequestURI())
                        .build());
    }

    // =========================================================================
    // 400 — IllegalArgumentException (used in auth / legacy code)
    // =========================================================================

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest req) {
        log.debug("IllegalArgumentException: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("INVALID_ARGUMENT")
                        .message(ex.getMessage())
                        .path(req.getRequestURI())
                        .build());
    }

    // =========================================================================
    // 401 — Token exceptions
    // =========================================================================

    @ExceptionHandler({TokenException.class, AuthException.class})
    ResponseEntity<ErrorResponse> handleTokenError(RuntimeException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .error("UNAUTHORIZED")
                        .message(ex.getMessage())
                        .path(req.getRequestURI())
                        .build());
    }

    // =========================================================================
    // 401 — Authentication
    // =========================================================================

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .error("UNAUTHORIZED")
                        .message("Authentification requise.")
                        .path(req.getRequestURI())
                        .build());
    }

    // =========================================================================
    // 403 — Authorisation
    // =========================================================================

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .error("FORBIDDEN")
                        .message("Accès refusé — droits insuffisants.")
                        .path(req.getRequestURI())
                        .build());
    }

    // =========================================================================
    // 500 — Unexpected
    // =========================================================================

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("INTERNAL_ERROR")
                        .message("Une erreur interne inattendue s'est produite.")
                        .path(req.getRequestURI())
                        .build());
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private String extractField(ConstraintViolation<?> cv) {
        String path = cv.getPropertyPath().toString();
        int dot = path.lastIndexOf('.');
        return dot >= 0 ? path.substring(dot + 1) : path;
    }
}
