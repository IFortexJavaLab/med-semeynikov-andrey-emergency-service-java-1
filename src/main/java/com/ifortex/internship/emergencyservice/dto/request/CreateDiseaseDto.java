package com.ifortex.internship.emergencyservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDiseaseDto(
    @NotNull(message = "Disease name is required")
    @NotEmpty(message = "Disease name can't be empty")
    @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
    String name
) {
}
