package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.UserDisease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserDiseaseRepository extends JpaRepository<UserDisease, UUID> {

    boolean existsByUserIdAndDiseaseId(UUID accountId, UUID diseaseId);

    boolean existsByUserIdAndCustomDiseaseIgnoreCase(UUID accountId, String customDisease);

    void deleteByUserIdAndId(UUID accountId, UUID diseaseId);

    List<UserDisease> findByUserId(UUID accountId);
}
