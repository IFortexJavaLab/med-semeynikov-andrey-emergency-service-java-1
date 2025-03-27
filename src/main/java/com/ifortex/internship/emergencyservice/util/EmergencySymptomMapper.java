package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.emergencyservice.dto.response.EmergencySymptomListDto;
import com.ifortex.internship.emergencyservice.model.Symptom;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencySymptom;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmergencySymptomMapper {

    public List<EmergencySymptomListDto> toTreeDto(List<EmergencySymptom> emergencySymptoms) {
        if (emergencySymptoms == null || emergencySymptoms.isEmpty()) {
            log.warn("Empty symptom list provided to mapper");
            return List.of();
        }

        List<Symptom> symptoms = emergencySymptoms.stream()
            .map(EmergencySymptom::getSymptom)
            .distinct()
            .toList();

        Map<UUID, List<Symptom>> childrenByParentId = symptoms.stream()
            .filter(s -> s.getParent() != null)
            .collect(Collectors.groupingBy(s -> s.getParent().getId()));

        List<Symptom> roots = symptoms.stream()
            .filter(s -> s.getParent() == null)
            .toList();

        log.debug("Mapping symptom tree with {} root nodes", roots.size());

        return roots.stream()
            .map(root -> mapSymptomToDtoWithChildren(root, childrenByParentId))
            .toList();
    }

    private EmergencySymptomListDto mapSymptomToDtoWithChildren(Symptom symptom, Map<UUID, List<Symptom>> childrenByParentId) {
        List<Symptom> children = childrenByParentId.getOrDefault(symptom.getId(), List.of());

        return new EmergencySymptomListDto(
            symptom.getId(),
            symptom.getName(),
            symptom.getType(),
            symptom.getParent() != null ? symptom.getParent().getId() : null,
            children.stream()
                .map(child -> mapSymptomToDtoWithChildren(child, childrenByParentId))
                .toList()
        );
    }
}