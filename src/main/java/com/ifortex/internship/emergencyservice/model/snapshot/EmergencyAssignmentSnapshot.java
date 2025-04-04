package com.ifortex.internship.emergencyservice.model.snapshot;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmergencyAssignmentSnapshot {

    UUID id;
    UUID emergencyId;
    String paramedicName;
    Instant assignedAt;
    Instant canceledAt;
    String cancellationComment;
    String cancellationReason;
}
