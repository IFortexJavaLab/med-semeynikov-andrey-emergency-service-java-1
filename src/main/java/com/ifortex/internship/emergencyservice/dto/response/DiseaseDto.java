package com.ifortex.internship.emergencyservice.dto.response;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.UUID;

public record DiseaseDto(

    @NotNull(message = "Disease ID is required")
    @UUID(message = "Must be a valid disease ID")
    String id,

    @NotNull(message = "Disease name is required")
    @NotEmpty(message = "Disease name can't be empty")
    @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
    String name

) {
}
