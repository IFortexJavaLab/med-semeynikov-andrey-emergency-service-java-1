package com.ifortex.internship.emergencyservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCustomAllergyRequest(
    @NotNull(message = "Name must not be null")
    @NotBlank(message = "Name must not be blank")
    @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
    String name
) {
}
