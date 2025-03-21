package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.emergencyservice.dto.response.UserAllergyDto;
import com.ifortex.internship.emergencyservice.model.UserAllergy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserAllergyMapper {

    @Mapping(target = "name", source = ".", qualifiedByName = "resolveAllergyName")
    UserAllergyDto toDto(UserAllergy entity);

    @Named("resolveAllergyName")
    static String resolveAllergyName(UserAllergy entity) {
        if (entity.getAllergy() != null && entity.getAllergy().getName() != null) {
            return entity.getAllergy().getName();
        }
        return entity.getCustomAllergy();
    }
}
