package com.ifortex.internship.emergencyservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AllergyDto(@NotNull(message = "Allergy ID is required")
                         @org.hibernate.validator.constraints.UUID(message = "Must be a valid allergy ID")
                         String id,
                         @NotNull(message = "Allergy name is required")
                         @NotEmpty(message = "Allergy name can't be empty")
                         String name) {
}
