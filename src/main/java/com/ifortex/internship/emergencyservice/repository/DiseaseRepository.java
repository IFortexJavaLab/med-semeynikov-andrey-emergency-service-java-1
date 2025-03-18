package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.Disease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiseaseRepository extends JpaRepository<Disease, Long> {

    Optional<Disease> findByDiseaseId(UUID uuid);

    Optional<Disease> findByName(String name);
}
