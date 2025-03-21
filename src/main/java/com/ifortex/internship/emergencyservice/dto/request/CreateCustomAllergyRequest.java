package com.ifortex.internship.emergencyservice.dto.request;

import com.ifortex.internship.medstarter.exception.custom.InvalidRequestException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCustomAllergyRequest(
    @NotNull(message = "Name must not be null")
    @NotBlank(message = "Name must not be blank")
    @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
    String name
) {
    public CreateCustomAllergyRequest {
        if (name == null) {
            throw new InvalidRequestException("Name must not be null");
        }
        if (name.isBlank()) {
            throw new InvalidRequestException("Name must not be blank");
        }
        if (name.isEmpty() || name.length() > 200) {
            throw new InvalidRequestException("Name must be between 1 and 200 characters");
        }
    }
}
