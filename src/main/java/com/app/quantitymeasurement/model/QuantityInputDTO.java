package com.app.quantitymeasurement.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request wrapper for all quantity operation endpoints.
 * {@code targetQuantityDTO} is optional — only needed for *-with-target-unit endpoints.
 */
@Data
@NoArgsConstructor
public class QuantityInputDTO {

    @Valid
    @NotNull(message = "First quantity (thisQuantityDTO) cannot be null")
    private QuantityDTO thisQuantityDTO;

    @Valid
    @NotNull(message = "Second quantity (thatQuantityDTO) cannot be null")
    private QuantityDTO thatQuantityDTO;

    /** Optional. Declares the desired result unit for add/subtract-with-target-unit. */
    @Valid
    private QuantityDTO targetQuantityDTO;
}