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
}
