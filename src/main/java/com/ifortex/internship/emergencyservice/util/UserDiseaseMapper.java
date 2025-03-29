package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.emergencyservice.dto.response.UserDiseaseDto;
import com.ifortex.internship.emergencyservice.model.UserDisease;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserDiseaseMapper {

    @Mapping(target = "name", source = ".", qualifiedByName = "resolveDiseaseByName")
    UserDiseaseDto toDto(UserDisease entity);

    List<UserDiseaseDto> toDtoList(List<UserDisease> diseases);

    @Named("resolveDiseaseByName")
    static String resolveDiseaseByName(UserDisease entity) {
        if (entity.getDisease() != null && entity.getDisease().getName() != null) {
            return entity.getDisease().getName();
        }
        return entity.getCustomDisease();
    }
}
