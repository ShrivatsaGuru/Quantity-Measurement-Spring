package com.app.quantitymeasurement.service;

import com.app.quantitymeasurement.exception.QuantityMeasurementException;
import com.app.quantitymeasurement.model.OperationType;
import com.app.quantitymeasurement.model.QuantityDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementEntity;
import com.app.quantitymeasurement.repository.QuantityMeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

/**
 * Business logic for all quantity measurement operations.
 *
 * Design notes:
 *  - Every operation (success OR error) is persisted so history endpoints always have data.
 *  - No class-level @Transactional — we intentionally save error records even on failure.
 *  - All arithmetic converts operands to a base unit first, then converts result to target unit.
 *
 * Base units: INCHES (Length), MILLILITER (Volume), GRAM (Weight), CELSIUS (Temperature)
 */
@Service
public class QuantityMeasurementServiceImpl implements IQuantityMeasurementService {

    private static final Logger log = Logger.getLogger(QuantityMeasurementServiceImpl.class.getName());

    @Autowired
    private QuantityMeasurementRepository repository;

    // ── COMPARE ───────────────────────────────────────────────────────────────

    @Override
    public QuantityMeasurementDTO compare(QuantityDTO thisQty, QuantityDTO thatQty) {
        QuantityMeasurementDTO result = new QuantityMeasurementDTO();
        try {
            assertSameType(thisQty, thatQty);
            boolean equal = Double.compare(toBase(thisQty), toBase(thatQty)) == 0;

            populate(result, thisQty, thatQty, OperationType.COMPARE);
            result.setResultString(String.valueOf(equal));   // "true" or "false"
        } catch (Exception e) {
            populateError(result, thisQty, thatQty, OperationType.COMPARE, e.getMessage());
        }
        return save(result);
    }

    // ── CONVERT ───────────────────────────────────────────────────────────────

    @Override
    public QuantityMeasurementDTO convert(QuantityDTO thisQty, QuantityDTO thatQty) {
        QuantityMeasurementDTO result = new QuantityMeasurementDTO();
        try {
            assertSameType(thisQty, thatQty);
            String targetUnit = thatQty.getUnit();
            double converted  = fromBase(toBase(thisQty), targetUnit, thisQty.getMeasurementType());

            populate(result, thisQty, thatQty, OperationType.CONVERT);
            result.setResultValue(converted);
            result.setResultUnit(targetUnit);
            result.setResultMeasurementType(thisQty.getMeasurementType());
            result.setResultString(thisQty.getValue() + " " + thisQty.getUnit()
                    + " = " + converted + " " + targetUnit);
        } catch (Exception e) {
            populateError(result, thisQty, thatQty, OperationType.CONVERT, e.getMessage());
        }
        return save(result);
    }

    // ── ADD ───────────────────────────────────────────────────────────────────

    @Override
    public QuantityMeasurementDTO add(QuantityDTO thisQty, QuantityDTO thatQty) {
        return arithmetic(thisQty, thatQty, null, OperationType.ADD);
    }

    @Override
    public QuantityMeasurementDTO add(QuantityDTO thisQty, QuantityDTO thatQty, QuantityDTO targetUnit) {
        return arithmetic(thisQty, thatQty, targetUnit, OperationType.ADD);
    }

    // ── SUBTRACT ──────────────────────────────────────────────────────────────

    @Override
    public QuantityMeasurementDTO subtract(QuantityDTO thisQty, QuantityDTO thatQty) {
        return arithmetic(thisQty, thatQty, null, OperationType.SUBTRACT);
    }

    @Override
    public QuantityMeasurementDTO subtract(QuantityDTO thisQty, QuantityDTO thatQty, QuantityDTO targetUnit) {
        return arithmetic(thisQty, thatQty, targetUnit, OperationType.SUBTRACT);
    }

    // ── MULTIPLY ──────────────────────────────────────────────────────────────

    @Override
    public QuantityMeasurementDTO multiply(QuantityDTO thisQty, QuantityDTO thatQty) {
        QuantityMeasurementDTO result = new QuantityMeasurementDTO();
        try {
            assertSameType(thisQty, thatQty);
            double product = toBase(thisQty) * toBase(thatQty);
            double finalVal = fromBase(product, thisQty.getUnit(), thisQty.getMeasurementType());

            populate(result, thisQty, thatQty, OperationType.MULTIPLY);
            result.setResultValue(finalVal);
            result.setResultUnit(thisQty.getUnit());
            result.setResultMeasurementType(thisQty.getMeasurementType());
            result.setResultString(thisQty.getValue() + " " + thisQty.getUnit()
                    + " * " + thatQty.getValue() + " " + thatQty.getUnit()
                    + " = " + finalVal + " " + thisQty.getUnit());
        } catch (Exception e) {
            populateError(result, thisQty, thatQty, OperationType.MULTIPLY, e.getMessage());
        }
        return save(result);
    }

    // ── DIVIDE ────────────────────────────────────────────────────────────────

    @Override
    public QuantityMeasurementDTO divide(QuantityDTO thisQty, QuantityDTO thatQty) {
        QuantityMeasurementDTO result = new QuantityMeasurementDTO();
        try {
            assertSameType(thisQty, thatQty);
            double divisor = toBase(thatQty);
            if (Double.compare(divisor, 0.0) == 0)
                throw new QuantityMeasurementException("Cannot divide by zero");

            double quotient = toBase(thisQty) / divisor;

            populate(result, thisQty, thatQty, OperationType.DIVIDE);
            result.setResultValue(quotient);
            result.setResultUnit(thisQty.getUnit());
            result.setResultMeasurementType(thisQty.getMeasurementType());
            result.setResultString(thisQty.getValue() + " " + thisQty.getUnit()
                    + " / " + thatQty.getValue() + " " + thatQty.getUnit()
                    + " = " + quotient);
        } catch (Exception e) {
            populateError(result, thisQty, thatQty, OperationType.DIVIDE, e.getMessage());
        }
        return save(result);
    }

    // ── HISTORY / ANALYTICS ───────────────────────────────────────────────────

    @Override
    public List<QuantityMeasurementDTO> getOperationHistory(String operation) {
        return QuantityMeasurementDTO.fromEntityList(
                repository.findByOperation(operation.toUpperCase()));
    }

    @Override
    public List<QuantityMeasurementDTO> getMeasurementsByType(String type) {
        return QuantityMeasurementDTO.fromEntityList(
                repository.findByThisMeasurementType(type));
    }

    @Override
    public long getOperationCount(String operation) {
        return repository.countByOperationAndIsErrorFalse(operation.toUpperCase());
    }

    @Override
    public List<QuantityMeasurementDTO> getErrorHistory() {
        return QuantityMeasurementDTO.fromEntityList(repository.findByIsErrorTrue());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Shared arithmetic handler for ADD and SUBTRACT (with or without a target unit). */
    private QuantityMeasurementDTO arithmetic(QuantityDTO thisQty, QuantityDTO thatQty,
                                               QuantityDTO targetUnit, OperationType op) {
        QuantityMeasurementDTO result = new QuantityMeasurementDTO();
        try {
            assertSameType(thisQty, thatQty);

            double baseResult = op == OperationType.ADD
                    ? toBase(thisQty) + toBase(thatQty)
                    : toBase(thisQty) - toBase(thatQty);

            String outUnit  = targetUnit != null ? targetUnit.getUnit() : thisQty.getUnit();
            double finalVal = fromBase(baseResult, outUnit, thisQty.getMeasurementType());
            String symbol   = op == OperationType.ADD ? "+" : "-";

            populate(result, thisQty, thatQty, op);
            result.setResultValue(finalVal);
            result.setResultUnit(outUnit);
            result.setResultMeasurementType(thisQty.getMeasurementType());
            result.setResultString(thisQty.getValue() + " " + thisQty.getUnit()
                    + " " + symbol + " " + thatQty.getValue() + " " + thatQty.getUnit()
                    + " = " + finalVal + " " + outUnit);
        } catch (Exception e) {
            populateError(result, thisQty, thatQty, op, e.getMessage());
        }
        return save(result);
    }

    /** Throws if the two quantities do not share the same measurement type. */
    private void assertSameType(QuantityDTO a, QuantityDTO b) {
        if (!a.getMeasurementType().equalsIgnoreCase(b.getMeasurementType()))
            throw new QuantityMeasurementException(
                    "Type mismatch: cannot operate on " + a.getMeasurementType()
                    + " and " + b.getMeasurementType());
    }

    /**
     * Converts a quantity to its base unit value.
     * Base units: INCHES (Length), MILLILITER (Volume), GRAM (Weight), CELSIUS (Temperature).
     */
    private double toBase(QuantityDTO qty) {
        double v = qty.getValue();
        return switch (qty.getMeasurementType()) {
            case "LengthUnit" -> switch (qty.getUnit().toUpperCase()) {
                case "FEET"        -> v * 12.0;
                case "YARDS"       -> v * 36.0;
                case "CENTIMETERS" -> v / 2.54;
                case "METERS"      -> v / 2.54 * 100;
                case "KILOMETERS"  -> v / 2.54 * 100_000;
                case "MILES"       -> v * 63_360;
                default            -> v;   // INCHES
            };
            case "VolumeUnit" -> switch (qty.getUnit().toUpperCase()) {
                case "LITRE"       -> v * 1_000.0;
                case "GALLON"      -> v * 3_785.41;
                case "CUBIC_METER" -> v * 1_000_000.0;
                default            -> v;   // MILLILITER
            };
            case "WeightUnit" -> switch (qty.getUnit().toUpperCase()) {
                case "KILOGRAM"    -> v * 1_000.0;
                case "TONNE"       -> v * 1_000_000.0;
                case "POUND"       -> v * 453.592;
                case "MILLIGRAM"   -> v / 1_000.0;
                default            -> v;   // GRAM
            };
            case "TemperatureUnit" -> switch (qty.getUnit().toUpperCase()) {
                case "FAHRENHEIT"  -> (v - 32) * 5.0 / 9.0;
                case "KELVIN"      -> v - 273.15;
                default            -> v;   // CELSIUS
            };
            default -> throw new QuantityMeasurementException(
                    "Unknown measurement type: " + qty.getMeasurementType());
        };
    }

    /** Converts a base-unit value back to the desired target unit. */
    private double fromBase(double base, String targetUnit, String type) {
        return switch (type) {
            case "LengthUnit" -> switch (targetUnit.toUpperCase()) {
                case "FEET"        -> base / 12.0;
                case "YARDS"       -> base / 36.0;
                case "CENTIMETERS" -> base * 2.54;
                case "METERS"      -> base * 2.54 / 100;
                case "KILOMETERS"  -> base * 2.54 / 100_000;
                case "MILES"       -> base / 63_360;
                default            -> base;  // INCHES
            };
            case "VolumeUnit" -> switch (targetUnit.toUpperCase()) {
                case "LITRE"       -> base / 1_000.0;
                case "GALLON"      -> base / 3_785.41;
                case "CUBIC_METER" -> base / 1_000_000.0;
                default            -> base;  // MILLILITER
            };
            case "WeightUnit" -> switch (targetUnit.toUpperCase()) {
                case "KILOGRAM"    -> base / 1_000.0;
                case "TONNE"       -> base / 1_000_000.0;
                case "POUND"       -> base / 453.592;
                case "MILLIGRAM"   -> base * 1_000.0;
                default            -> base;  // GRAM
            };
            case "TemperatureUnit" -> switch (targetUnit.toUpperCase()) {
                case "FAHRENHEIT"  -> base * 9.0 / 5.0 + 32;
                case "KELVIN"      -> base + 273.15;
                default            -> base;  // CELSIUS
            };
            default -> throw new QuantityMeasurementException("Unknown type: " + type);
        };
    }

    /** Fills in the common operand + operation fields on the result DTO. */
    private void populate(QuantityMeasurementDTO dto,
                          QuantityDTO thisQty, QuantityDTO thatQty, OperationType op) {
        dto.setThisValue(thisQty.getValue());        dto.setThisUnit(thisQty.getUnit());
        dto.setThisMeasurementType(thisQty.getMeasurementType());
        dto.setThatValue(thatQty.getValue());        dto.setThatUnit(thatQty.getUnit());
        dto.setThatMeasurementType(thatQty.getMeasurementType());
        dto.setOperation(op.name());
        dto.setError(false);
    }

    /** Fills in error fields on the result DTO. Always called before save(). */
    private void populateError(QuantityMeasurementDTO dto,
                                QuantityDTO thisQty, QuantityDTO thatQty,
                                OperationType op, String message) {
        log.warning(op + " error: " + message);
        if (thisQty != null) populate(dto, thisQty, thatQty, op);
        dto.setOperation(op.name());
        dto.setError(true);
        dto.setErrorMessage(message);
    }

    /** Persists the result entity and returns the saved DTO. */
    private QuantityMeasurementDTO save(QuantityMeasurementDTO dto) {
        try {
            QuantityMeasurementEntity saved = repository.save(dto.toEntity());
            return QuantityMeasurementDTO.fromEntity(saved);
        } catch (Exception e) {
            log.severe("DB save failed: " + e.getMessage());
            return dto;
        }
    }
}
