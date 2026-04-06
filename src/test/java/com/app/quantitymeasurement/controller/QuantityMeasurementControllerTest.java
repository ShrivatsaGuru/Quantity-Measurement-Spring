package com.app.quantitymeasurement.controller;

import com.app.quantitymeasurement.model.QuantityDTO;
import com.app.quantitymeasurement.model.QuantityInputDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementDTO;
import com.app.quantitymeasurement.service.IQuantityMeasurementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for the controller layer only.
 * Service is mocked — verifies HTTP wiring, request parsing, and response format.
 */
@WebMvcTest(QuantityMeasurementController.class)
class QuantityMeasurementControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper json;
    @MockBean  private IQuantityMeasurementService service;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private QuantityDTO qty(double v, String unit, String type) {
        return new QuantityDTO(v, unit, type);
    }

    private QuantityInputDTO input(QuantityDTO a, QuantityDTO b) {
        QuantityInputDTO dto = new QuantityInputDTO();
        dto.setThisQuantityDTO(a);
        dto.setThatQuantityDTO(b);
        return dto;
    }

    private QuantityMeasurementDTO response(String op, double result, String unit, String resultStr) {
        QuantityMeasurementDTO dto = new QuantityMeasurementDTO();
        dto.setOperation(op);
        dto.setResultValue(result);
        dto.setResultUnit(unit);
        dto.setResultString(resultStr);
        dto.setError(false);
        return dto;
    }

    // ── POST /compare ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /compare → 200 with resultString 'true'")
    void compare_returns200() throws Exception {
        QuantityMeasurementDTO stub = new QuantityMeasurementDTO();
        stub.setResultString("true");
        stub.setOperation("COMPARE");

        when(service.compare(any(), any())).thenReturn(stub);

        mockMvc.perform(post("/api/v1/quantities/compare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(input(
                        qty(1, "FEET", "LengthUnit"),
                        qty(12, "INCHES", "LengthUnit")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("COMPARE"))
                .andExpect(jsonPath("$.resultString").value("true"))
                .andExpect(jsonPath("$.error").value(false));
    }

    // ── POST /add ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /add → 200 with resultValue and resultUnit")
    void add_returns200() throws Exception {
        when(service.add(any(), any()))
                .thenReturn(response("ADD", 2.0, "FEET", "1.0 FEET + 12.0 INCHES = 2.0 FEET"));

        mockMvc.perform(post("/api/v1/quantities/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(input(
                        qty(1, "FEET", "LengthUnit"),
                        qty(12, "INCHES", "LengthUnit")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("ADD"))
                .andExpect(jsonPath("$.resultValue").value(2.0))
                .andExpect(jsonPath("$.resultUnit").value("FEET"))
                .andExpect(jsonPath("$.resultString").value("1.0 FEET + 12.0 INCHES = 2.0 FEET"));
    }

    // ── POST /subtract ────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /subtract → 200")
    void subtract_returns200() throws Exception {
        when(service.subtract(any(), any()))
                .thenReturn(response("SUBTRACT", 1.0, "FEET", "2.0 FEET - 12.0 INCHES = 1.0 FEET"));

        mockMvc.perform(post("/api/v1/quantities/subtract")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(input(
                        qty(2, "FEET", "LengthUnit"),
                        qty(12, "INCHES", "LengthUnit")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("SUBTRACT"))
                .andExpect(jsonPath("$.resultValue").value(1.0));
    }

    // ── POST /multiply ────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /multiply → 200")
    void multiply_returns200() throws Exception {
        when(service.multiply(any(), any()))
                .thenReturn(response("MULTIPLY", 72.0, "FEET", "2.0 FEET * 3.0 FEET = 72.0 FEET"));

        mockMvc.perform(post("/api/v1/quantities/multiply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(input(
                        qty(2, "FEET", "LengthUnit"),
                        qty(3, "FEET", "LengthUnit")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("MULTIPLY"))
                .andExpect(jsonPath("$.resultValue").value(72.0));
    }

    // ── POST /divide ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /divide → 200 with ratio")
    void divide_returns200() throws Exception {
        when(service.divide(any(), any()))
                .thenReturn(response("DIVIDE", 2.0, "FEET", "2.0 FEET / 1.0 FEET = 2.0"));

        mockMvc.perform(post("/api/v1/quantities/divide")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(input(
                        qty(2, "FEET", "LengthUnit"),
                        qty(1, "FEET", "LengthUnit")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultValue").value(2.0));
    }

    // ── Validation error ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /add with missing thisQuantityDTO → 400 Validation Error")
    void add_missingBody_returns400() throws Exception {
        QuantityInputDTO bad = new QuantityInputDTO();
        bad.setThatQuantityDTO(qty(12, "INCHES", "LengthUnit"));
        // thisQuantityDTO is null → @NotNull violation

        mockMvc.perform(post("/api/v1/quantities/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    // ── GET /count ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /count/ADD → 200 with long value")
    void count_returns200() throws Exception {
        when(service.getOperationCount("ADD")).thenReturn(7L);

        mockMvc.perform(get("/api/v1/quantities/count/ADD"))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
    }

    // ── GET /history/operation ────────────────────────────────────────────────

    @Test
    @DisplayName("GET /history/operation/COMPARE → 200 with list")
    void historyByOperation_returns200() throws Exception {
        when(service.getOperationHistory("COMPARE")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/quantities/history/operation/COMPARE"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // ── GET /history/errored ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /history/errored → 200 with list")
    void erroredHistory_returns200() throws Exception {
        when(service.getErrorHistory()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/quantities/history/errored"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
