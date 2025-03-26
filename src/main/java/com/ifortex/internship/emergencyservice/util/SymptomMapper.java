package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.emergencyservice.dto.response.SymptomDto;
import com.ifortex.internship.emergencyservice.model.Symptom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueMappingStrategy;

import java.util.List;
import java.util.UUID;

@Mapper(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL, componentModel = "spring")
public interface SymptomMapper {

    @Mapping(target = "parentId", source = "parent", qualifiedByName = "symptomToUUID")
    SymptomDto toDto(Symptom symptom);

    List<SymptomDto> toListDtos(List<Symptom> symptoms);

    @Named("symptomToUUID")
    static UUID symptomToUUID(Symptom parent) {
        return parent != null ? parent.getId() : null;
    }
}
