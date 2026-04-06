package com.app.quantitymeasurement.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Represents a single measurable quantity sent in the request body.
 * Validation ensures unit belongs to the declared measurement type.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuantityDTO {

    private static final String TYPE_REGEX =
            "LengthUnit|VolumeUnit|WeightUnit|TemperatureUnit";

    // Valid units per category
    private static final Set<String> LENGTH_UNITS =
            Set.of("FEET", "INCHES", "YARDS", "CENTIMETERS", "METERS", "KILOMETERS", "MILES");
    private static final Set<String> VOLUME_UNITS =
            Set.of("LITRE", "MILLILITER", "GALLON", "CUBIC_METER");
    private static final Set<String> WEIGHT_UNITS =
            Set.of("GRAM", "KILOGRAM", "MILLIGRAM", "POUND", "TONNE");
    private static final Set<String> TEMPERATURE_UNITS =
            Set.of("CELSIUS", "FAHRENHEIT", "KELVIN");

    @NotNull(message = "Value cannot be null")
    private Double value;

    @NotNull(message = "Unit cannot be null")
    private String unit;

    @NotNull(message = "Measurement type cannot be null")
    @Pattern(
        regexp = TYPE_REGEX,
        message = "Measurement type must be: LengthUnit | VolumeUnit | WeightUnit | TemperatureUnit"
    )
    private String measurementType;

    /** Cross-field validation: unit must belong to the declared measurementType. */
    @AssertTrue(message = "Unit is not valid for the specified measurement type")
    public boolean isValidUnit() {
        if (unit == null || measurementType == null) return true; // @NotNull handles nulls
        return switch (measurementType) {
            case "LengthUnit"      -> LENGTH_UNITS.contains(unit.toUpperCase());
            case "VolumeUnit"      -> VOLUME_UNITS.contains(unit.toUpperCase());
            case "WeightUnit"      -> WEIGHT_UNITS.contains(unit.toUpperCase());
            case "TemperatureUnit" -> TEMPERATURE_UNITS.contains(unit.toUpperCase());
            default                -> false;
        };
    }
}