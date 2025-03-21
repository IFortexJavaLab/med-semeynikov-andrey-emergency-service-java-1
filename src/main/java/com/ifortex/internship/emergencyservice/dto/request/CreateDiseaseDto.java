package com.ifortex.internship.emergencyservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateDiseaseDto(@NotNull(message = "Disease name is required")
                               @NotEmpty(message = "Disease name can't be empty")
                               String name) {
}
