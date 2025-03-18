package com.ifortex.internship.emergencyservice.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ifortex.internship.emergencyservice.model.constant.SymptomType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SymptomCreate(
    @NotNull(message = "Symptom name is required")
    @NotEmpty(message = "Symptom name can't be empty")
    String name,
    String advice,
    @NotNull(message = "Symptom type is required")
    SymptomType type,
    String animationKey,
    @org.hibernate.validator.constraints.UUID(message = "Must be a valid symptom ID")
    String parentId) {
}
