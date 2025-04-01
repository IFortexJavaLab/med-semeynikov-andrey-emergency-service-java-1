package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.emergency.EmergencyResolutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergencyResolutionRepository extends JpaRepository<EmergencyResolutionEntity, Long> {
}
