package com.ifortex.internship.emergencyservice.model.snapshot;

import com.ifortex.internship.emergencyservice.model.constant.CancellationReason;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmergencyAssignmentSnapshot {

    UUID id;
    UUID emergencyId;
    UUID paramedicId;
    Instant assignedAt;
    Instant canceledAt;
    String cancellationComment;
    CancellationReason cancellationReason;
}
