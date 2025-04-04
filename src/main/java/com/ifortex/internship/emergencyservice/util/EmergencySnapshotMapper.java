package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.emergencyservice.dto.response.AdminEmergencyViewDto;
import com.ifortex.internship.emergencyservice.dto.response.ClientEmergencyViewDto;
import com.ifortex.internship.emergencyservice.dto.response.EmergencySymptomListDto;
import com.ifortex.internship.emergencyservice.dto.response.ParamedicEmergencyViewDto;
import com.ifortex.internship.emergencyservice.dto.response.SymptomDto;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencySnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmergencySnapshotMapper {

    @Mapping(target = "symptoms", source = "symptoms", qualifiedByName = "buildSymptomTree")
    @Mapping(target = "userDiseases", source = "diseases")
    @Mapping(target = "userAllergies", source = "allergies")
    ParamedicEmergencyViewDto toParamedicViewDtoOngoing(EmergencySnapshot snapshot);

    @Mapping(target = "symptoms", ignore = true)
    @Mapping(target = "userDiseases", ignore = true)
    @Mapping(target = "userAllergies", ignore = true)
    ParamedicEmergencyViewDto toParamedicViewDtoFinished(EmergencySnapshot snapshot);

    @Mapping(target = "feedback", source = "feedback")
    @Mapping(target = "symptoms", source = "symptoms", qualifiedByName = "buildSymptomTree")
    @Mapping(target = "duration", expression = "java(resolveDuration(snapshot.getDuration(), snapshot.getCreatedAt()))")
    ClientEmergencyViewDto toClientViewDto(EmergencySnapshot snapshot);

    @Mapping(target = "symptoms", source = "symptoms", qualifiedByName = "buildSymptomTree")
    AdminEmergencyViewDto toAdminDto(EmergencySnapshot snapshot);

    @Named("buildSymptomTree")
    default List<EmergencySymptomListDto> buildSymptomTree(List<SymptomDto> flatList) {
        if (flatList == null) {
            return List.of();
        }

        Map<UUID, EmergencySymptomListDto> idToNode = new HashMap<>();
        List<EmergencySymptomListDto> roots = new ArrayList<>();

        for (SymptomDto dto : flatList) {
            idToNode.put(dto.id(), new EmergencySymptomListDto(
                dto.id(),
                dto.name(),
                dto.type(),
                dto.advice(),
                dto.animationKey(),
                dto.parentId(),
                new ArrayList<>()
            ));
        }

        for (EmergencySymptomListDto node : idToNode.values()) {
            if (node.parentId() != null && idToNode.containsKey(node.parentId())) {
                idToNode.get(node.parentId()).children().add(node);
            } else {
                roots.add(node);
            }
        }

        return roots;
    }

    default Duration resolveDuration(Duration duration, Instant createdAt) {
        if (duration != null) {
            return duration;
        }
        return Duration.between(createdAt, Instant.now());
    }
}