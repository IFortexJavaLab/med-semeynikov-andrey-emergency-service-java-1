package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.request.SymptomCreate;
import com.ifortex.internship.emergencyservice.dto.request.SymptomUpdate;
import com.ifortex.internship.emergencyservice.dto.response.SymptomDto;
import com.ifortex.internship.emergencyservice.model.Symptom;
import com.ifortex.internship.emergencyservice.model.constant.SymptomType;
import com.ifortex.internship.emergencyservice.repository.SymptomRepository;
import com.ifortex.internship.emergencyservice.util.SymptomMapper;
import com.ifortex.internship.medstarter.exception.custom.DuplicateResourceException;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import com.ifortex.internship.medstarter.exception.custom.InvalidRequestException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SymptomService {

    public static final String LOG_SYMPTOM_WITH_NAME_IS_ALREADY_EXISTS = "Symptom with name : {} is already exists";

    SymptomMapper symptomMapper;
    SymptomRepository symptomRepository;

    @Transactional
    public void createSymptom(SymptomCreate symptomCreate) {
        String name = symptomCreate.name();
        log.debug("Creating new symptom with name: {}", name);

        if (symptomRepository.findByName(symptomCreate.name()).isPresent()) {
            log.error(LOG_SYMPTOM_WITH_NAME_IS_ALREADY_EXISTS, name);
            throw new DuplicateResourceException(String.format("Symptom with name %s already exists.", name));
        }

        SymptomType newType = symptomCreate.type();
        Symptom parentSymptom = null;

        if (symptomCreate.parentId() != null) {
            UUID parentId = UUID.fromString(symptomCreate.parentId());
            parentSymptom = getSymptomById(parentId);

            List<Symptom> siblings = parentSymptom.getChildren();
            for (Symptom sibling : siblings) {
                if (!sibling.getType().equals(newType)) {
                    log.error("Invalid input type: {} for the symptom with name: {}. Mixed input types within same level are not allowed.",
                        symptomCreate.type(),
                        symptomCreate.name());
                    throw new InvalidRequestException(
                        String.format("Invalid input type: %s. Mixed input types within same level are not allowed.", symptomCreate.type()));
                }
            }

        }

        Symptom newSymptom = new Symptom()
            .setName(name)
            .setType(symptomCreate.type())
            .setAdvice(symptomCreate.advice())
            .setAnimationKey(symptomCreate.animationKey())
            .setParent(parentSymptom);

        symptomRepository.save(newSymptom);
        log.info("Symptom with name: {} and ID: {} created successfully", name, newSymptom.getId());
    }

    public List<SymptomDto> getRootSymptoms(int page, int size) {
        log.debug("Fetching root symptoms. Page: {}, Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Symptom> rootSymptoms = symptomRepository.findByParentIsNull(pageable);
        return symptomMapper.toListDtos(rootSymptoms.stream().toList());
    }

    public List<SymptomDto> getChildSymptoms(UUID parentId, int page, int size) {
        log.debug("Fetching child symptoms for parentId: {}. Page: {}, Size: {}", parentId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Symptom> childSymptoms = symptomRepository.findByParentId(parentId, pageable);
        return symptomMapper.toListDtos(childSymptoms.stream().toList());
    }

    @Transactional
    public void updateSymptom(SymptomUpdate symptomUpdate) {
        log.debug("Attempt to update symptom with ID: {}", symptomUpdate.id());

        Optional<Symptom> duplicate = symptomRepository.findByName(symptomUpdate.name());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(symptomUpdate.idAsUUID())) {
            log.error(LOG_SYMPTOM_WITH_NAME_IS_ALREADY_EXISTS, symptomUpdate.name());
            throw new DuplicateResourceException(String.format("Symptom with name %s already exists.", symptomUpdate.name()));
        }

        Symptom savedSymptom = getSymptomById(symptomUpdate.idAsUUID());
        SymptomType newType = symptomUpdate.type();

        UUID newParentId = symptomUpdate.parentId() != null ? symptomUpdate.parentIdAsUUID() : null;
        Symptom newParent = null;

        if (newParentId != null) {
            newParent = getSymptomById(newParentId);

            if (isCircularReference(savedSymptom, newParent)) {
                log.error("Cannot assign parent with ID: {}. Circular reference detected. Symptom to update: {}",
                    symptomUpdate.parentId(),
                    symptomUpdate.id());
                throw new InvalidRequestException(
                    String.format("Cannot assign parent with ID: %s. Circular reference detected. Symptom to update: %s",
                        symptomUpdate.parentId(),
                        symptomUpdate.id()));
            }

            List<Symptom> siblings = newParent.getChildren().stream()
                .filter(s -> !s.getId().equals(savedSymptom.getId()))
                .toList();

            for (Symptom sibling : siblings) {
                if (!sibling.getType().equals(newType)) {
                    log.error("Cannot assign to parent with ID: {}. Sibling symptoms have different type: {}. Symptom to update: {}",
                        symptomUpdate.parentId(),
                        symptomUpdate.type(),
                        symptomUpdate.id());
                    throw new InvalidRequestException(
                        String.format("Cannot assign to parent with ID: %s. Sibling symptoms have different type: %s. Symptom to update: %s",
                            symptomUpdate.parentId(),
                            symptomUpdate.type(),
                            symptomUpdate.id()));
                }
            }
        }

        savedSymptom.setName(symptomUpdate.name())
            .setAdvice(symptomUpdate.advice())
            .setType(newType)
            .setAnimationKey(symptomUpdate.animationKey())
            .setParent(newParent);

        symptomRepository.save(savedSymptom);
        log.info("Symptom with ID: {} updated successfully", symptomUpdate.id());
    }

    @Transactional
    public void deleteSymptom(UUID symptomId) {
        log.info("Deleting symptom with ID: {}", symptomId);

        Symptom symptom = getSymptomById(symptomId);
        Set<UUID> descendants = findAllDescendants(symptom);
        symptomRepository.deleteAllById(descendants);

        log.info("Deleted symptom {} and {} descendants", symptomId, descendants.size() - 1);
    }

    public List<SymptomDto> getPotentialParents(UUID symptomId, int page, int size) {
        log.debug("Fetching potential parent symptoms for symptom ID: {}", symptomId);

        Symptom symptom = getSymptomById(symptomId);
        Set<UUID> descendants = findAllDescendants(symptom);

        Pageable pageable = PageRequest.of(page, size);
        Page<Symptom> potentialParents = symptomRepository.findByIdNotIn(descendants, pageable);
        return symptomMapper.toListDtos(potentialParents.stream().toList());
    }

    public SymptomDto getSymptomDto(UUID symptomId) {
        log.debug("Fetching symptom: [{}]", symptomId);
        return symptomMapper.toDto(getSymptomById(symptomId));
    }

    public Symptom getSymptomById(UUID symptomId) {
        log.info("Retrieving symptom by ID: {}", symptomId);
        return symptomRepository.findById(symptomId)
            .orElseThrow(() -> {
                log.error("Symptom with ID {} not found", symptomId);
                return new EntityNotFoundException(String.format("Symptom with ID: %s not found", symptomId));
            });
    }

    private boolean isCircularReference(Symptom symptom, Symptom potentialParent) {
        log.debug("Checking is circular reference exists for symptom: {} and new parent symptom: {}", symptom.getId(), potentialParent.getId());
        while (potentialParent != null) {
            if (potentialParent.getId().equals(symptom.getId())) {
                return true;
            }
            potentialParent = potentialParent.getParent();
        }
        return false;
    }

    private Set<UUID> findAllDescendants(Symptom symptom) {
        Set<UUID> descendants = new HashSet<>();
        Queue<Symptom> queue = new LinkedList<>();
        queue.add(symptom);

        while (!queue.isEmpty()) {
            Symptom current = queue.poll();
            descendants.add(current.getId());

            List<Symptom> children = symptomRepository.findByParentId(current.getId());
            queue.addAll(children);
        }
        return descendants;
    }

}
