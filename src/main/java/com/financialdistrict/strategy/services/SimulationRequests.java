package com.financialdistrict.strategy.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Shared request-field validation for the simulation endpoints: a missing
// field should read as a 400 with a clear message, not an unboxing NPE
// surfacing as a 500.
final class SimulationRequests {

    private SimulationRequests() {
    }

    static <T> T require(T value, String fieldName) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value;
    }
}
