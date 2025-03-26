package com.ifortex.internship.emergencyservice.dto.response;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;

public record AllergyDto(

    @NotNull(message = "Disease ID is required")
    @UUID(message = "Must be a valid disease ID")
    String id,

    @NotNull(message = "Disease name is required")
    @NotEmpty(message = "Disease name can't be empty")
    String name

) {
    public AllergyDto {
        if (id == null) {
            throw new IllegalArgumentException("Disease ID must not be null");
        }
        try {
            java.util.UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Disease ID must be a valid UUID", e);
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Disease name must not be null or empty");
        }
    }
}
