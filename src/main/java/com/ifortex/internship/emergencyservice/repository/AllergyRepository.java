package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Long> {

    Optional<Allergy> findByAllergyId(UUID allergyId);

    Optional<Allergy> findByName(String name);
}
