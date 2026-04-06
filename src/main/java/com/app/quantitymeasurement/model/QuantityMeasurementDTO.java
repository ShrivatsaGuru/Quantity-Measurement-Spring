package com.app.quantitymeasurement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * API response DTO for every quantity measurement operation.
 * Produced from QuantityMeasurementEntity via static factory methods.
 *
 * Static helpers:
 *   fromEntity(entity)       — entity → DTO
 *   fromEntityList(entities) — List&lt;entity&gt; → List&lt;DTO&gt;
 *   toEntity()               — DTO → entity (for saving)
 */
@Data
@NoArgsConstructor
public class QuantityMeasurementDTO {

    // ── Operands ─────────────────────────────────────────────────────────────
    private double thisValue;
    private String thisUnit;
    private String thisMeasurementType;
    private double thatValue;
    private String thatUnit;
    private String thatMeasurementType;

    // ── Operation & result ────────────────────────────────────────────────────
    private String operation;
    private double resultValue;
    private String resultUnit;
    private String resultMeasurementType;
    /** "true"/"false" for compare; human-readable expression for arithmetic. */
    private String resultString;

    // ── Error state ───────────────────────────────────────────────────────────
    /** @JsonProperty ensures "error" in JSON (Lombok would generate "error" too, but explicit is safer). */
    @JsonProperty("error")
    private boolean error;
    private String errorMessage;

    // ── Entity ↔ DTO conversion ───────────────────────────────────────────────

    /** Converts a persisted entity to this response DTO. */
    public static QuantityMeasurementDTO fromEntity(QuantityMeasurementEntity e) {
        if (e == null) return null;
        QuantityMeasurementDTO dto = new QuantityMeasurementDTO();
        dto.thisValue             = e.getThisValue();
        dto.thisUnit              = e.getThisUnit();
        dto.thisMeasurementType   = e.getThisMeasurementType();
        dto.thatValue             = e.getThatValue();
        dto.thatUnit              = e.getThatUnit();
        dto.thatMeasurementType   = e.getThatMeasurementType();
        dto.operation             = e.getOperation();
        dto.resultValue           = e.getResultValue();
        dto.resultUnit            = e.getResultUnit();
        dto.resultMeasurementType = e.getResultMeasurementType();
        dto.resultString          = e.getResultString();
        dto.error                 = e.isError();
        dto.errorMessage          = e.getErrorMessage();
        return dto;
    }

    /** Converts this DTO to a new entity ready for saving. Timestamps set by @PrePersist. */
    public QuantityMeasurementEntity toEntity() {
        QuantityMeasurementEntity e = new QuantityMeasurementEntity();
        e.setThisValue(thisValue);              e.setThisUnit(thisUnit);
        e.setThisMeasurementType(thisMeasurementType);
        e.setThatValue(thatValue);              e.setThatUnit(thatUnit);
        e.setThatMeasurementType(thatMeasurementType);
        e.setOperation(operation);
        e.setResultValue(resultValue);          e.setResultUnit(resultUnit);
        e.setResultMeasurementType(resultMeasurementType);
        e.setResultString(resultString);
        e.setError(error);                      e.setErrorMessage(errorMessage);
        return e;
    }

    /** Converts a list of entities to DTOs. */
    public static List<QuantityMeasurementDTO> fromEntityList(List<QuantityMeasurementEntity> entities) {
        return entities.stream().map(QuantityMeasurementDTO::fromEntity).collect(Collectors.toList());
    }
}