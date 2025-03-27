package com.ifortex.internship.emergencyservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ifortex.internship.emergencyservice.model.constant.SymptomType;

import java.util.List;
import java.util.UUID;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public record EmergencySymptomListDto(
    UUID id,
    String name,
    SymptomType type,
    UUID parentId,
    List<EmergencySymptomListDto> children) {
}
