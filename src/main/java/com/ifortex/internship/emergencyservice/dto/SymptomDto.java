package com.ifortex.internship.emergencyservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ifortex.internship.emergencyservice.model.constant.SymptomType;

import java.util.UUID;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public record SymptomDto(
    UUID id,
    String name,
    SymptomType type,
    String advice,
    String animationKey,
    UUID parentId) {
}
