package com.app.quantitymeasurement.service;

import com.app.quantitymeasurement.model.QuantityDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementDTO;

import java.util.List;

/**
 * Service contract for all quantity measurement operations.
 * The controller depends only on this interface, keeping it easy to mock in tests.
 */
public interface IQuantityMeasurementService {

    QuantityMeasurementDTO compare(QuantityDTO thisQty, QuantityDTO thatQty);

    QuantityMeasurementDTO convert(QuantityDTO thisQty, QuantityDTO thatQty);

    QuantityMeasurementDTO add(QuantityDTO thisQty, QuantityDTO thatQty);

    /** Add with explicit target unit — result expressed in targetUnit's unit. */
    QuantityMeasurementDTO add(QuantityDTO thisQty, QuantityDTO thatQty, QuantityDTO targetUnit);

    QuantityMeasurementDTO subtract(QuantityDTO thisQty, QuantityDTO thatQty);

    /** Subtract with explicit target unit — result expressed in targetUnit's unit. */
    QuantityMeasurementDTO subtract(QuantityDTO thisQty, QuantityDTO thatQty, QuantityDTO targetUnit);

    QuantityMeasurementDTO multiply(QuantityDTO thisQty, QuantityDTO thatQty);

    QuantityMeasurementDTO divide(QuantityDTO thisQty, QuantityDTO thatQty);

    // ── History / analytics ───────────────────────────────────────────────────
    List<QuantityMeasurementDTO> getOperationHistory(String operation);
    List<QuantityMeasurementDTO> getMeasurementsByType(String type);
    long                         getOperationCount(String operation);
    List<QuantityMeasurementDTO> getErrorHistory();
}