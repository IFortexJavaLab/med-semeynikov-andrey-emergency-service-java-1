package com.ifortex.internship.emergencyservice.dto.request;

import com.ifortex.internship.medstarter.exception.custom.InvalidRequestException;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateDiseaseDto(
    @NotNull(message = "Disease name is required")
    @NotEmpty(message = "Disease name can't be empty")
    String name
) {
    public CreateDiseaseDto {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidRequestException("Disease name must not be null or empty");
        }
    }
}
