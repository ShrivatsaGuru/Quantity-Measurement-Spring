package com.app.quantitymeasurement;

import com.app.quantitymeasurement.model.QuantityDTO;
import com.app.quantitymeasurement.model.QuantityInputDTO;
import com.app.quantitymeasurement.model.QuantityMeasurementDTO;
import com.app.quantitymeasurement.repository.QuantityMeasurementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration test — loads the complete Spring context with H2 in-memory DB.
 * Verifies that operations are persisted and history/count endpoints reflect the data.
 */
@SpringBootTest
@AutoConfigureMockMvc
class QuantityMeasurementIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper json;
    @Autowired private QuantityMeasurementRepository repository;

    @BeforeEach
    void clearDb() {
        repository.deleteAll();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private QuantityDTO qty(double v, String unit, String type) {
        return new QuantityDTO(v, unit, type);
    }

    private String body(QuantityDTO a, QuantityDTO b) throws Exception {
        QuantityInputDTO dto = new QuantityInputDTO();
        dto.setThisQuantityDTO(a);
        dto.setThatQuantityDTO(b);
        return json.writeValueAsString(dto);
    }

    // ── End-to-end operation + persistence flow ───────────────────────────────

    @Test
    @DisplayName("POST /add persists a record; GET /count/ADD returns 1")
    void add_persistsRecord_countReturns1() throws Exception {
        mockMvc.perform(post("/api/v1/quantities/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(qty(1, "FEET", "LengthUnit"),
                              qty(12, "INCHES", "LengthUnit"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultValue").value(2.0))
                .andExpect(jsonPath("$.error").value(false));

        mockMvc.perform(get("/api/v1/quantities/count/ADD"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("POST /compare: 1 KILOGRAM == 1000 GRAM → true, persisted")
    void compare_kilogramEqualsGram_true() throws Exception {
        mockMvc.perform(post("/api/v1/quantities/compare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(qty(1, "KILOGRAM", "WeightUnit"),
                              qty(1000, "GRAM", "WeightUnit"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultString").value("true"));

        assertThat(repository.findByOperation("COMPARE")).hasSize(1);
    }

    @Test
    @DisplayName("POST /convert: 0 CELSIUS → FAHRENHEIT = 32")
    void convert_zeroCelsiusToFahrenheit() throws Exception {
        mockMvc.perform(post("/api/v1/quantities/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(qty(0, "CELSIUS", "TemperatureUnit"),
                              qty(0, "FAHRENHEIT", "TemperatureUnit"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultValue").value(32.0))
                .andExpect(jsonPath("$.resultUnit").value("FAHRENHEIT"));
    }

    @Test
    @DisplayName("POST /divide by zero → error flag set and record persisted")
    void divideByZero_errored_persisted() throws Exception {
        mockMvc.perform(post("/api/v1/quantities/divide")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(qty(10, "FEET", "LengthUnit"),
                              qty(0, "FEET", "LengthUnit"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(true));

        mockMvc.perform(get("/api/v1/quantities/history/errored"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /history/type/LengthUnit returns only length operations")
    void historyByType_filtersCorrectly() throws Exception {
        // Add a length operation
        mockMvc.perform(post("/api/v1/quantities/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(qty(1, "FEET", "LengthUnit"), qty(1, "FEET", "LengthUnit"))))
                .andExpect(status().isOk());

        // Add a weight operation
        mockMvc.perform(post("/api/v1/quantities/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(qty(1, "KILOGRAM", "WeightUnit"), qty(1, "KILOGRAM", "WeightUnit"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/quantities/history/type/LengthUnit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].thisMeasurementType").value("LengthUnit"));
    }

    @Test
    @DisplayName("POST /multiply → result persisted correctly")
    void multiply_resultPersisted() throws Exception {
        mockMvc.perform(post("/api/v1/quantities/multiply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(qty(2, "KILOGRAM", "WeightUnit"),
                              qty(3, "KILOGRAM", "WeightUnit"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("MULTIPLY"))
                .andExpect(jsonPath("$.error").value(false));

        assertThat(repository.findByOperation("MULTIPLY")).hasSize(1);
    }
}
