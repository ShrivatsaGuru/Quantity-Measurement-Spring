package com.app.quantitymeasurement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity persisted for every operation (success or error) — used for history/audit.
 * Never sent over the wire; use QuantityMeasurementDTO for API responses.
 *
 * NOTE: @Getter/@Setter used instead of @Data to avoid Lombok generating
 *       equals()/hashCode() on all fields — that breaks JPA identity semantics.
 */
@Entity
@Table(
    name = "quantity_measurement_entity",
    indexes = {
        @Index(name = "idx_operation",        columnList = "operation"),
        @Index(name = "idx_measurement_type", columnList = "this_measurement_type"),
        @Index(name = "idx_created_at",       columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class QuantityMeasurementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── First operand ─────────────────────────────────────────────────────────
    @Column(name = "this_value",            nullable = false) private double thisValue;
    @Column(name = "this_unit",             nullable = false) private String thisUnit;
    @Column(name = "this_measurement_type", nullable = false) private String thisMeasurementType;

    // ── Second operand ────────────────────────────────────────────────────────
    @Column(name = "that_value",            nullable = false) private double thatValue;
    @Column(name = "that_unit",             nullable = false) private String thatUnit;
    @Column(name = "that_measurement_type", nullable = false) private String thatMeasurementType;

    // ── Operation & result ────────────────────────────────────────────────────
    @Column(name = "operation",               nullable = false) private String operation;
    @Column(name = "result_value")                             private double resultValue;
    @Column(name = "result_unit")                              private String resultUnit;
    @Column(name = "result_measurement_type")                  private String resultMeasurementType;
    /** Human-readable result e.g. "1.0 FEET + 12.0 INCHES = 2.0 FEET" or "true" for compare. */
    @Column(name = "result_string")                            private String resultString;

    // ── Error state ───────────────────────────────────────────────────────────
    @Column(name = "is_error")      private boolean isError;
    @Column(name = "error_message") private String  errorMessage;

    // ── Timestamps ────────────────────────────────────────────────────────────
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)                    private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}