package com.ifortex.internship.emergencyservice.dto.request;

import java.util.UUID;

public record EntityIdRequest(
    @org.hibernate.validator.constraints.UUID(message = "Must be a valid symptom ID")
    String id
) {
    public UUID asUUID() {
        return UUID.fromString(id);
    }
}
