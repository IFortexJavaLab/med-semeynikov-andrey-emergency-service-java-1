package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmergencyLocationRepository extends JpaRepository<EmergencyLocation, UUID> {

    void deleteByEmergencyIdAndLocationType(UUID id, EmergencyLocationType emergencyLocationType);
}
