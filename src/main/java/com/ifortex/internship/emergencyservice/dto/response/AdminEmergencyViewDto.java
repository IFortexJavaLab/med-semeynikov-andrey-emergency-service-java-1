package com.ifortex.internship.emergencyservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencyAssignmentSnapshot;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencyFeedbackSnapshot;
import com.ifortex.internship.emergencyservice.model.snapshot.ParamedicEmergencyLocationSnapshot;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminEmergencyViewDto {
    String id;
    Instant createdAt;
    Instant closedAt;
    Duration duration;
    EmergencyStatus status;

    String clientFirstName;
    String paramedicName;

    BigDecimal latitude;
    BigDecimal longitude;

    List<ParamedicEmergencyLocationSnapshot> paramedicLocations;
    List<EmergencyAssignmentSnapshot> assignments;

    String resolution;
    String resolutionExplanation;

    EmergencyFeedbackSnapshot feedback;
    List<EmergencySymptomListDto> symptoms;
    List<UserAllergyDto> allergies;
    List<UserDiseaseDto> diseases;

    ParamedicLocationViewDto currentParamedicLocation;
}