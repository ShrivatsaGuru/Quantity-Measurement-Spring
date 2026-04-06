package com.app.quantitymeasurement.service;

import com.app.quantitymeasurement.exception.QuantityMeasurementException;
import com.app.quantitymeasurement.model.QuantityDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementEntity;
import com.app.quantitymeasurement.repository.QuantityMeasurementRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QuantityMeasurementServiceImpl.
 * Repository is mocked — no Spring context needed.
 */
@ExtendWith(MockitoExtension.class)
class QuantityMeasurementServiceImplTest {

    @Mock
    private QuantityMeasurementRepository repository;

    @InjectMocks
    private QuantityMeasurementServiceImpl service;

    // Helper: stub repository.save() to return the entity it receives (with id=1)
    @BeforeEach
    void stubSave() {
        when(repository.save(any(QuantityMeasurementEntity.class)))
            .thenAnswer(inv -> {
                QuantityMeasurementEntity e = inv.getArgument(0);
                e.setId(1L);
                return e;
            });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private QuantityDTO qty(double v, String unit, String type) {
        return new QuantityDTO(v, unit, type);
    }

    // ── COMPARE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("compare: 1 FEET == 12 INCHES → true")
    void compare_equalLengths_returnsTrue() {
        QuantityMeasurementDTO r = service.compare(
                qty(1, "FEET", "LengthUnit"),
                qty(12, "INCHES", "LengthUnit"));

        assertThat(r.getResultString()).isEqualTo("true");
        assertThat(r.isError()).isFalse();
        assertThat(r.getOperation()).isEqualTo("COMPARE");
    }

    @Test
    @DisplayName("compare: 1 FEET != 1 INCHES → false")
    void compare_unequalLengths_returnsFalse() {
        QuantityMeasurementDTO r = service.compare(
                qty(1, "FEET", "LengthUnit"),
                qty(1, "INCHES", "LengthUnit"));

        assertThat(r.getResultString()).isEqualTo("false");
        assertThat(r.isError()).isFalse();
    }

    @Test
    @DisplayName("compare: type mismatch → error saved")
    void compare_typeMismatch_setsError() {
        QuantityMeasurementDTO r = service.compare(
                qty(1, "FEET", "LengthUnit"),
                qty(1, "LITRE", "VolumeUnit"));

        assertThat(r.isError()).isTrue();
        assertThat(r.getErrorMessage()).contains("mismatch");
        verify(repository).save(any());  // error record must still be persisted
    }

    // ── CONVERT ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("convert: 1 FEET → INCHES = 12")
    void convert_feetToInches() {
        QuantityMeasurementDTO r = service.convert(
                qty(1, "FEET", "LengthUnit"),
                qty(0, "INCHES", "LengthUnit"));

        assertThat(r.getResultValue()).isEqualTo(12.0);
        assertThat(r.getResultUnit()).isEqualTo("INCHES");
        assertThat(r.isError()).isFalse();
    }

    @Test
    @DisplayName("convert: 1 KILOGRAM → GRAM = 1000")
    void convert_kilogramToGram() {
        QuantityMeasurementDTO r = service.convert(
                qty(1, "KILOGRAM", "WeightUnit"),
                qty(0, "GRAM", "WeightUnit"));

        assertThat(r.getResultValue()).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("convert: 100 CELSIUS → FAHRENHEIT = 212")
    void convert_celsiusToFahrenheit() {
        QuantityMeasurementDTO r = service.convert(
                qty(100, "CELSIUS", "TemperatureUnit"),
                qty(0, "FAHRENHEIT", "TemperatureUnit"));

        assertThat(r.getResultValue()).isCloseTo(212.0, within(0.001));
    }

    @Test
    @DisplayName("convert: 1 LITRE → MILLILITER = 1000")
    void convert_litreToMilliliter() {
        QuantityMeasurementDTO r = service.convert(
                qty(1, "LITRE", "VolumeUnit"),
                qty(0, "MILLILITER", "VolumeUnit"));

        assertThat(r.getResultValue()).isEqualTo(1000.0);
    }

    // ── ADD ───────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("add: 1 FEET + 12 INCHES = 2 FEET")
    void add_feetAndInches_resultInFeet() {
        QuantityMeasurementDTO r = service.add(
                qty(1, "FEET", "LengthUnit"),
                qty(12, "INCHES", "LengthUnit"));

        assertThat(r.getResultValue()).isEqualTo(2.0);
        assertThat(r.getResultUnit()).isEqualTo("FEET");
        assertThat(r.getResultString()).contains("2.0 FEET");
        assertThat(r.isError()).isFalse();
    }

    @Test
    @DisplayName("add-with-target: 1 FEET + 12 INCHES in CENTIMETERS")
    void add_withTargetUnit_resultInCentimeters() {
        QuantityMeasurementDTO r = service.add(
                qty(1, "FEET", "LengthUnit"),
                qty(12, "INCHES", "LengthUnit"),
                qty(0, "CENTIMETERS", "LengthUnit"));

        // 2 FEET = 24 INCHES = 24 × 2.54 = 60.96 cm
        assertThat(r.getResultValue()).isCloseTo(60.96, within(0.01));
        assertThat(r.getResultUnit()).isEqualTo("CENTIMETERS");
    }

    @Test
    @DisplayName("add: type mismatch → error")
    void add_typeMismatch_setsError() {
        QuantityMeasurementDTO r = service.add(
                qty(1, "FEET", "LengthUnit"),
                qty(1, "LITRE", "VolumeUnit"));

        assertThat(r.isError()).isTrue();
    }

    // ── SUBTRACT ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("subtract: 2 FEET - 12 INCHES = 1 FEET")
    void subtract_feetAndInches() {
        QuantityMeasurementDTO r = service.subtract(
                qty(2, "FEET", "LengthUnit"),
                qty(12, "INCHES", "LengthUnit"));

        assertThat(r.getResultValue()).isEqualTo(1.0);
        assertThat(r.getResultUnit()).isEqualTo("FEET");
    }

    @Test
    @DisplayName("subtract-with-target: 2 KILOGRAM - 500 GRAM in GRAM = 1500")
    void subtract_withTargetUnit() {
        QuantityMeasurementDTO r = service.subtract(
                qty(2, "KILOGRAM", "WeightUnit"),
                qty(500, "GRAM", "WeightUnit"),
                qty(0, "GRAM", "WeightUnit"));

        assertThat(r.getResultValue()).isCloseTo(1500.0, within(0.001));
        assertThat(r.getResultUnit()).isEqualTo("GRAM");
    }

    // ── MULTIPLY ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("multiply: 2 FEET * 3 FEET (both in INCHES base, result in FEET)")
    void multiply_twoLengths() {
        QuantityMeasurementDTO r = service.multiply(
                qty(2, "FEET", "LengthUnit"),
                qty(3, "FEET", "LengthUnit"));

        // 2 FEET = 24 INCHES, 3 FEET = 36 INCHES → product = 864 base² → back to FEET = 864/12 = 72
        assertThat(r.getResultValue()).isCloseTo(72.0, within(0.001));
        assertThat(r.isError()).isFalse();
    }

    // ── DIVIDE ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("divide: 2 FEET / 1 FEET = 2 (dimensionless ratio)")
    void divide_normal() {
        QuantityMeasurementDTO r = service.divide(
                qty(2, "FEET", "LengthUnit"),
                qty(1, "FEET", "LengthUnit"));

        assertThat(r.getResultValue()).isEqualTo(2.0);
        assertThat(r.isError()).isFalse();
    }

    @Test
    @DisplayName("divide by zero → error persisted")
    void divide_byZero_setsError() {
        QuantityMeasurementDTO r = service.divide(
                qty(10, "FEET", "LengthUnit"),
                qty(0, "FEET", "LengthUnit"));

        assertThat(r.isError()).isTrue();
        assertThat(r.getErrorMessage()).containsIgnoringCase("zero");
        verify(repository).save(any());
    }

    // ── HISTORY ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getOperationHistory: delegates to repository and maps to DTOs")
    void getOperationHistory_returnsMappedList() {
        when(repository.findByOperation("ADD")).thenReturn(List.of());
        List<QuantityMeasurementDTO> result = service.getOperationHistory("add");
        assertThat(result).isEmpty();
        verify(repository).findByOperation("ADD");  // normalised to uppercase
    }

    @Test
    @DisplayName("getOperationCount: delegates correctly")
    void getOperationCount_delegatesToRepository() {
        when(repository.countByOperationAndIsErrorFalse("COMPARE")).thenReturn(5L);
        assertThat(service.getOperationCount("compare")).isEqualTo(5L);
    }

    @Test
    @DisplayName("getErrorHistory: returns only errored records")
    void getErrorHistory_returnsErroredRecords() {
        when(repository.findByIsErrorTrue()).thenReturn(List.of());
        assertThat(service.getErrorHistory()).isEmpty();
        verify(repository).findByIsErrorTrue();
    }
}
