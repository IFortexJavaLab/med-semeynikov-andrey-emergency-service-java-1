package com.ifortex.internship.emergencyservice.dto.request;

import com.ifortex.internship.medstarter.exception.custom.InvalidRequestException;

import java.util.UUID;

public record EntityIdRequest(
    @org.hibernate.validator.constraints.UUID(message = "Must be a valid symptom ID")
    String id
) {
    public EntityIdRequest {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidRequestException("Must be a valid allergy ID");
        }
    }

    public UUID asUUID() {
        return UUID.fromString(id);
    }

}
