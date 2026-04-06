package com.app.quantitymeasurement.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Centralised exception handler for all controllers.
 *
 * Handlers:
 *   1. MethodArgumentNotValidException — @Valid failures       → HTTP 400
 *   2. QuantityMeasurementException    — domain errors         → HTTP 400
 *   3. Exception                       — everything else       → HTTP 500
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = Logger.getLogger(GlobalExceptionHandler.class.getName());

    /** Consistent error response shape returned on every failure. */
    record ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {}

    // 1. Bean Validation failures (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getAllErrors()
                .stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList());
        log.warning("Validation failed: " + errors);
        return build(HttpStatus.BAD_REQUEST, "Validation Error", String.join("; ", errors), "request body");
    }

    // 2. Domain exceptions (incompatible types, divide-by-zero, unknown unit, …)
    @ExceptionHandler(QuantityMeasurementException.class)
    public ResponseEntity<ErrorResponse> handleDomain(QuantityMeasurementException ex,
                                                       HttpServletRequest req) {
        log.warning("Domain error at " + req.getRequestURI() + ": " + ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Quantity Measurement Error", ex.getMessage(), req.getRequestURI());
    }

    // 3. Catch-all (unexpected exceptions)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest req) {
        log.severe("Unhandled exception at " + req.getRequestURI() + ": " + ex.getMessage());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), req.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message, String path) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(LocalDateTime.now(), status.value(), error, message, path));
    }
}