package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.emergencyservice.dto.DiseaseDto;
import com.ifortex.internship.emergencyservice.model.Disease;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DiseaseMapper {

    @Mapping(target = "diseaseId", source = "diseaseId", qualifiedByName = "uuidToString")
    DiseaseDto toDto(Disease disease);

    List<DiseaseDto> toListDtos(List<Disease> diseases);

    @Named("uuidToString")
    static String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }
}
