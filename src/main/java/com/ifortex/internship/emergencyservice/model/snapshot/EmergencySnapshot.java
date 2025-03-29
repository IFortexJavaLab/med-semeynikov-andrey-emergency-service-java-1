package com.ifortex.internship.emergencyservice.model.snapshot;

import com.ifortex.internship.emergencyservice.dto.response.SymptomDto;
import com.ifortex.internship.emergencyservice.dto.response.UserAllergyDto;
import com.ifortex.internship.emergencyservice.dto.response.UserDiseaseDto;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyResolution;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Document(collection = "emergency_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmergencySnapshot {

    @Id
    String id;
    Instant createdAt;
    Instant closedAt;
    EmergencyStatus status;

    UUID clientId;
    UUID paramedicId;

    List<EmergencyLocationSnapshot> locations;
    List<EmergencyCancellationSnapshot> cancellations;
    List<EmergencyAssignmentSnapshot> assignments;

    String resolutionExplanation;
    EmergencyResolution resolution;
    EmergencyFeedbackSnapshot feedback;

    List<SymptomDto> symptoms;
    List<UserAllergyDto> allergies;
    List<UserDiseaseDto> diseases;
}
