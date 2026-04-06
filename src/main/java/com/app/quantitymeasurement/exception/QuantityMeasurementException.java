package com.app.quantitymeasurement.exception;

/** Thrown for domain-level errors (incompatible types, divide by zero, etc.). Caught as HTTP 400. */
public class QuantityMeasurementException extends RuntimeException {

    public QuantityMeasurementException(String message) {
        super(message);
    }

    public QuantityMeasurementException(String message, Throwable cause) {
        super(message, cause);
    }
}