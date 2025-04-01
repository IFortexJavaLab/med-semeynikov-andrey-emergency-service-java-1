package com.ifortex.internship.emergencyservice.dto.response;

import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParamedicEmergencyViewDto {

    String id;
    Instant createdAt;
    Instant closedAt;
    EmergencyStatus status;
    String clientFirstName;
    BigDecimal latitude;
    BigDecimal longitude;
    List<EmergencySymptomListDto> symptoms;
    List<UserDiseaseDto> userDiseases;
    List<UserAllergyDto> userAllergies;
}
