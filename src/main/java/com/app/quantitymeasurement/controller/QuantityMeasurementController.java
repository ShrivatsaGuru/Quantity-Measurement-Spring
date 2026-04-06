package com.app.quantitymeasurement.controller;

import com.app.quantitymeasurement.model.QuantityInputDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementDTO;
import com.app.quantitymeasurement.service.IQuantityMeasurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

/**
 * REST controller for all quantity measurement endpoints.
 *
 * Base path: /api/v1/quantities
 *
 * POST  /compare                   — compare two quantities (returns true/false)
 * POST  /convert                   — convert to a target unit
 * POST  /add                       — add two quantities
 * POST  /add-with-target-unit      — add and express result in a given unit
 * POST  /subtract                  — subtract two quantities
 * POST  /subtract-with-target-unit — subtract and express result in a given unit
 * POST  /multiply                  — multiply two quantities
 * POST  /divide                    — divide two quantities
 *
 * GET   /history/operation/{op}    — all records for an operation type
 * GET   /history/type/{type}       — all records for a measurement type
 * GET   /history/errored           — all error records
 * GET   /count/{operation}         — count of successful operations
 */
@RestController
@RequestMapping("/api/v1/quantities")
@Tag(name = "Quantity Measurements", description = "REST API for quantity measurement operations")
public class QuantityMeasurementController {

    private static final Logger log = Logger.getLogger(QuantityMeasurementController.class.getName());

    @Autowired
    private IQuantityMeasurementService service;

    // ── POST — operations ─────────────────────────────────────────────────────

    @PostMapping("/compare")
    @Operation(summary = "Compare two quantities",
               description = "Returns resultString='true' if equal after unit conversion, 'false' otherwise")
    public ResponseEntity<QuantityMeasurementDTO> compare(@Valid @RequestBody QuantityInputDTO input) {
        log.info("POST /compare");
        return ResponseEntity.ok(service.compare(input.getThisQuantityDTO(), input.getThatQuantityDTO()));
    }

    @PostMapping("/convert")
    @Operation(summary = "Convert a quantity to a target unit",
               description = "Converts thisQuantityDTO to the unit declared in thatQuantityDTO")
    public ResponseEntity<QuantityMeasurementDTO> convert(@Valid @RequestBody QuantityInputDTO input) {
        log.info("POST /convert");
        return ResponseEntity.ok(service.convert(input.getThisQuantityDTO(), input.getThatQuantityDTO()));
    }

    @PostMapping("/add")
    @Operation(summary = "Add two quantities",
               description = "Result expressed in thisQuantityDTO's unit")
    public ResponseEntity<QuantityMeasurementDTO> add(@Valid @RequestBody QuantityInputDTO input) {
        log.info("POST /add");
        return ResponseEntity.ok(service.add(input.getThisQuantityDTO(), input.getThatQuantityDTO()));
    }

    @PostMapping("/add-with-target-unit")
    @Operation(summary = "Add two quantities and express result in a target unit",
               description = "targetQuantityDTO.unit declares the desired result unit")
    public ResponseEntity<QuantityMeasurementDTO> addWithTargetUnit(@Valid @RequestBody QuantityInputDTO input) {
        log.info("POST /add-with-target-unit");
        return ResponseEntity.ok(service.add(
                input.getThisQuantityDTO(), input.getThatQuantityDTO(), input.getTargetQuantityDTO()));
    }

    @PostMapping("/subtract")
    @Operation(summary = "Subtract two quantities",
               description = "thatQuantityDTO is subtracted from thisQuantityDTO; result in thisQuantityDTO's unit")
    public ResponseEntity<QuantityMeasurementDTO> subtract(@Valid @RequestBody QuantityInputDTO input) {
        log.info("POST /subtract");
        return ResponseEntity.ok(service.subtract(input.getThisQuantityDTO(), input.getThatQuantityDTO()));
    }

    @PostMapping("/subtract-with-target-unit")
    @Operation(summary = "Subtract two quantities and express result in a target unit",
               description = "targetQuantityDTO.unit declares the desired result unit")
    public ResponseEntity<QuantityMeasurementDTO> subtractWithTargetUnit(@Valid @RequestBody QuantityInputDTO input) {
        log.info("POST /subtract-with-target-unit");
        return ResponseEntity.ok(service.subtract(
                input.getThisQuantityDTO(), input.getThatQuantityDTO(), input.getTargetQuantityDTO()));
    }

    @PostMapping("/multiply")
    @Operation(summary = "Multiply two quantities",
               description = "Both operands are converted to base unit before multiplying")
    public ResponseEntity<QuantityMeasurementDTO> multiply(@Valid @RequestBody QuantityInputDTO input) {
        log.info("POST /multiply");
        return ResponseEntity.ok(service.multiply(input.getThisQuantityDTO(), input.getThatQuantityDTO()));
    }

    @PostMapping("/divide")
    @Operation(summary = "Divide two quantities",
               description = "Returns a dimensionless ratio; throws 400 if divisor is zero")
    public ResponseEntity<QuantityMeasurementDTO> divide(@Valid @RequestBody QuantityInputDTO input) {
        log.info("POST /divide");
        return ResponseEntity.ok(service.divide(input.getThisQuantityDTO(), input.getThatQuantityDTO()));
    }

    // ── GET — history / analytics ─────────────────────────────────────────────

    @GetMapping("/history/operation/{operation}")
    @Operation(summary = "Get history by operation type",
               description = "Valid values: COMPARE, CONVERT, ADD, SUBTRACT, MULTIPLY, DIVIDE")
    public ResponseEntity<List<QuantityMeasurementDTO>> historyByOperation(
            @PathVariable String operation) {
        log.info("GET /history/operation/" + operation);
        return ResponseEntity.ok(service.getOperationHistory(operation));
    }

    @GetMapping("/history/type/{type}")
    @Operation(summary = "Get history by measurement type",
               description = "Valid values: LengthUnit, VolumeUnit, WeightUnit, TemperatureUnit")
    public ResponseEntity<List<QuantityMeasurementDTO>> historyByType(
            @PathVariable String type) {
        log.info("GET /history/type/" + type);
        return ResponseEntity.ok(service.getMeasurementsByType(type));
    }

    @GetMapping("/history/errored")
    @Operation(summary = "Get all errored operations",
               description = "Returns all persisted records where error = true")
    public ResponseEntity<List<QuantityMeasurementDTO>> erroredHistory() {
        log.info("GET /history/errored");
        return ResponseEntity.ok(service.getErrorHistory());
    }

    @GetMapping("/count/{operation}")
    @Operation(summary = "Get count of successful operations",
               description = "Returns the number of non-error records for the given operation type")
    public ResponseEntity<Long> operationCount(@PathVariable String operation) {
        log.info("GET /count/" + operation);
        return ResponseEntity.ok(service.getOperationCount(operation));
    }
}