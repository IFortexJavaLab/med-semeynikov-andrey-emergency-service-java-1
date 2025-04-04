package com.ifortex.internship.emergencyservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParamedicEmergencyViewDto {

    String id;
    Instant createdAt;
    Instant closedAt;
    Duration duration;
    EmergencyStatus status;
    String clientFirstName;
    BigDecimal latitude;
    BigDecimal longitude;
    List<EmergencySymptomListDto> symptoms;
    List<UserDiseaseDto> userDiseases;
    List<UserAllergyDto> userAllergies;

    //todo add BonusApplied in the future
}
