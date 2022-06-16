package com.jmunoz.hoaxify.shared;

import lombok.Data;
import lombok.NoArgsConstructor;

// Refactor
@Data
@NoArgsConstructor
public class GenericResponse {
    private String message;

    public GenericResponse(String message) {
        this.message = message;
    }
}
