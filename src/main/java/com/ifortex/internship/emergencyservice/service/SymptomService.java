package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.SymptomDto;
import com.ifortex.internship.emergencyservice.dto.request.SymptomCreate;
import com.ifortex.internship.emergencyservice.dto.request.SymptomUpdate;
import com.ifortex.internship.emergencyservice.model.Symptom;
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
        log.debug("Creating new symptom with name: {}", symptomCreate.name());

        if (symptomRepository.findByName(symptomCreate.name()).isPresent()) {
            log.error(LOG_SYMPTOM_WITH_NAME_IS_ALREADY_EXISTS, symptomCreate.name());
            throw new DuplicateResourceException(String.format("Symptom with name %s already exists.", symptomCreate.name()));
        }

        Symptom newSymptom = new Symptom()
            .setName(symptomCreate.name())
            .setType(symptomCreate.type())
            .setAdvice(symptomCreate.advice())
            .setAnimationKey(symptomCreate.animationKey());

        if (symptomCreate.parentId() != null) {
            UUID parentId = UUID.fromString(symptomCreate.parentId());
            var parentSymptom = getSymptomById(parentId);
            newSymptom.setParent(parentSymptom);
        }

        symptomRepository.save(newSymptom);
        log.info("Symptom with name: {} and ID: {} created successfully", newSymptom.getName(), newSymptom.getId());
    }

    public List<SymptomDto> getAllRootSymptoms(int page, int size) {
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

        if (symptomRepository.findByName(symptomUpdate.name()).isPresent()) {
            log.error(LOG_SYMPTOM_WITH_NAME_IS_ALREADY_EXISTS, symptomUpdate.name());
            throw new DuplicateResourceException(String.format("Symptom with name %s already exists.", symptomUpdate.name()));
        }

        Symptom savedSymptom = getSymptomById(UUID.fromString(symptomUpdate.id()));

        UUID newParentId = symptomUpdate.parentId() != null ? UUID.fromString(symptomUpdate.parentId()) : null;
        savedSymptom.setParent(null);
        if (newParentId != null) {
            Symptom newParent = getSymptomById(newParentId);

            if (isCircularReference(savedSymptom, newParent)) {
                log.error("Circular reference detected! Cannot set symptom: {} as a parent of symptom {}", newParentId, savedSymptom.getId());
                throw new InvalidRequestException(
                    String.format("Cannot set symptom: %s as a parent of symptom %s", newParentId, savedSymptom.getId()));
            }
            savedSymptom.setParent(newParent);
        }

        savedSymptom.setName(symptomUpdate.name())
            .setAdvice(symptomUpdate.advice())
            .setType(symptomUpdate.type())
            .setAnimationKey(symptomUpdate.animationKey());

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
