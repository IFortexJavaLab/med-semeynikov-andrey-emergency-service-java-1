package com.ifortex.internship.emergencyservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateAllergyDto(
    @NotNull(message = "Allergy name is required")
    @NotEmpty(message = "Allergy name can't be empty")
    String name
) {
}
