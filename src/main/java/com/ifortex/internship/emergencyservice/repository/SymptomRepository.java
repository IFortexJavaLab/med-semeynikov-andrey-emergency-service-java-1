package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.Symptom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, UUID> {

    Page<Symptom> findByParentIsNull(Pageable pageable);

    Page<Symptom> findByParentId(UUID parentId, Pageable pageable);

    List<Symptom> findByParentId(UUID parentId);

    Page<Symptom> findByIdNotIn(Set<UUID> ids, Pageable pageable);

    Optional<Symptom> findByName(String name);
}
