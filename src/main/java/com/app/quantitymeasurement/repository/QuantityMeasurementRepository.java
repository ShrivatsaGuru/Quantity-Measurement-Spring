package com.app.quantitymeasurement.repository;

import com.app.quantitymeasurement.model.QuantityMeasurementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository — all queries are auto-generated from method names
 * or declared via @Query. No boilerplate implementation needed.
 */
@Repository
public interface QuantityMeasurementRepository
        extends JpaRepository<QuantityMeasurementEntity, Long> {

    // All records for a given operation type (e.g. "ADD", "COMPARE")
    List<QuantityMeasurementEntity> findByOperation(String operation);

    // All records where the first operand had a specific measurement type
    List<QuantityMeasurementEntity> findByThisMeasurementType(String type);

    // Count of successful (non-error) records for the given operation
    long countByOperationAndIsErrorFalse(String operation);

    // All records that resulted in an error
    List<QuantityMeasurementEntity> findByIsErrorTrue();

    // Custom JPQL: successful records for an operation (used by history endpoint)
    @Query("SELECT e FROM QuantityMeasurementEntity e WHERE e.operation = :op AND e.isError = false")
    List<QuantityMeasurementEntity> findSuccessfulByOperation(@Param("op") String operation);
}