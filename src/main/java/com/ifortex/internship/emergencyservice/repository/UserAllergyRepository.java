package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.UserAllergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAllergyRepository extends JpaRepository<UserAllergy, UUID> {

    boolean existsByUserIdAndAllergyId(UUID userId, UUID allergyId);

    boolean existsByUserIdAndCustomAllergyIgnoreCase(UUID userId, String customAllergy);

    void deleteByUserIdAndId(UUID userId, UUID allergyId);

    List<UserAllergy> findByUserId(UUID userId);
}
