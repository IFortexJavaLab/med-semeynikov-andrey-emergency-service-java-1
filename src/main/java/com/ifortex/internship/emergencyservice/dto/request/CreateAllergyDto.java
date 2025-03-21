package com.ifortex.internship.emergencyservice.dto.request;

import com.ifortex.internship.medstarter.exception.custom.InvalidRequestException;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateAllergyDto(
    @NotNull(message = "Allergy name is required")
    @NotEmpty(message = "Allergy name can't be empty")
    String name
) {
    public CreateAllergyDto {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidRequestException("Allergy name must not be null or empty");
        }
    }
}
