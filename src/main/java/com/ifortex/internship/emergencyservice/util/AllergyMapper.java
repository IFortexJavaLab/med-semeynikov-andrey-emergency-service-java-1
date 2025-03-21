package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.emergencyservice.dto.AllergyDto;
import com.ifortex.internship.emergencyservice.model.Allergy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AllergyMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
    AllergyDto toDto(Allergy allergy);

    List<AllergyDto> toListDtos(List<Allergy> allergies);

    @Named("uuidToString")
    static String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }
}
