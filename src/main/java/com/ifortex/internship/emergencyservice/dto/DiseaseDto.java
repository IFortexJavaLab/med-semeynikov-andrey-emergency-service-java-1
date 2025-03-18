package com.ifortex.internship.emergencyservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record DiseaseDto(@NotNull(message = "Disease ID is required")
                         @org.hibernate.validator.constraints.UUID(message = "Must be a valid allergy ID")
                         String diseaseId,
                         @NotNull(message = "Allergy name is required")
                         @NotEmpty(message = "Allergy name can't be empty")
                         String name) {
}
