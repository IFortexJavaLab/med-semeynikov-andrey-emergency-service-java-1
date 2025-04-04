package com.ifortex.internship.emergencyservice.dto.response;

import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
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
public class ClientEmergencyViewDto {
    String id;
    EmergencyStatus status;
    BigDecimal latitude;
    BigDecimal longitude;
    Instant createdAt;
    Instant closedAt;
    Duration duration;
    String resolution;
    String resolutionExplanation;
    String paramedicName;
    ParamedicLocationViewDto lastParamedicLocation;
    List<EmergencySymptomListDto> symptoms;
    FeedbackDto feedback;
}
