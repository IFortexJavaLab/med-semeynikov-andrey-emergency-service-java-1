package com.ifortex.internship.emergencyservice.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ifortex.internship.emergencyservice.model.constant.SymptomType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SymptomUpdate(

    @UUID(message = "Must be a valid symptom ID")
    String id,

    @NotNull(message = "Symptom name is required")
    @NotEmpty(message = "Symptom name can't be empty")
    String name,

    @Size(min = 1, max = 200, message = "Advice must be between 1 and 200 characters")
    String advice,

    SymptomType type,

    @Size(min = 1, max = 200, message = "Animation key must be between 1 and 200 characters")
    String animationKey,

    @UUID(message = "Must be a valid symptom ID")
    String parentId

) {
    public SymptomUpdate {
        if (id == null) {
            throw new IllegalArgumentException("Symptom ID must not be null");
        }
        try {
            java.util.UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Symptom ID must be a valid UUID", e);
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Symptom name must not be null or empty");
        }

        if (advice != null && (advice.isEmpty() || advice.length() > 200)) {
            throw new IllegalArgumentException("Advice must be between 1 and 200 characters");
        }

        if (animationKey != null && (animationKey.isEmpty() || animationKey.length() > 200)) {
            throw new IllegalArgumentException("Animation key must be between 1 and 200 characters");
        }

        if (parentId != null) {
            try {
                java.util.UUID.fromString(parentId);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Parent ID must be a valid UUID", e);
            }
        }
    }
}
