# Quantity Measurement App

A Spring Boot REST API for performing quantity measurement operations — compare, convert, add, subtract, and divide across Length, Volume, Weight, and Temperature units. All operations are persisted to a database for full audit and history support.

## Overview

UC17 transforms a standalone Java quantity measurement application (UC16) into a production-ready Spring Boot REST service. All original business logic is preserved. The persistence layer is upgraded from raw JDBC to Spring Data JPA, and all functionality is exposed through RESTful HTTP endpoints with JSON responses.

Key capabilities:

- Compare two quantities of the same type (e.g. 1 FOOT vs 12 INCHES → `true`)
- Convert a quantity to a different unit (e.g. 100°C → 212°F)
- Add, subtract, and divide quantities with optional target-unit output
- Retrieve full operation history by type, measurement category, or error status
- Auto-generated Swagger UI documentation
- Spring Actuator health and metrics endpoints
- In-memory H2 database for development; MySQL-ready for production

---

## Technology stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Persistence | Spring Data JPA + Hibernate |
| Database (dev) | H2 in-memory |
| Database (prod) | MySQL 8 |
| Validation | Jakarta Bean Validation |
| Security | Spring Security (CORS + permit-all for dev) |
| API docs | SpringDoc OpenAPI / Swagger UI |
| Monitoring | Spring Boot Actuator |
| Boilerplate reduction | Lombok |
| Build tool | Maven |
| Testing | JUnit 5, Mockito, MockMvc, TestRestTemplate |

### Request and response shapes

#### Request body — `QuantityInputDTO`

All POST endpoints share the same request body shape. `targetQuantityDTO` is optional and only required for `add-with-target-unit` and `subtract-with-target-unit`.

```json
{
  "thisQuantityDTO": {
    "value": 1.0,
    "unit": "FEET",
    "measurementType": "LengthUnit"
  },
  "thatQuantityDTO": {
    "value": 12.0,
    "unit": "INCHES",
    "measurementType": "LengthUnit"
  },
  "targetQuantityDTO": {
    "value": 0.0,
    "unit": "INCHES",
    "measurementType": "LengthUnit"
  }
}
```

#### Response body — `QuantityMeasurementDTO`

All POST endpoints return the same response shape. Fields not applicable to the operation (e.g. `resultString` for arithmetic operations) will be `null` or `0.0`.

```json
{
  "thisValue": 1.0,
  "thisUnit": "FEET",
  "thisMeasurementType": "LengthUnit",
  "thatValue": 12.0,
  "thatUnit": "INCHES",
  "thatMeasurementType": "LengthUnit",
  "operation": "COMPARE",
  "resultString": "true",
  "resultValue": 0.0,
  "resultUnit": null,
  "resultMeasurementType": null,
  "error": false,
  "errorMessage": null
}
```
