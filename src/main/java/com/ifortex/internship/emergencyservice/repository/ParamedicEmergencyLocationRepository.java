package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.emergency.ParamedicEmergencyLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParamedicEmergencyLocationRepository extends JpaRepository<ParamedicEmergencyLocation, Long> {
}
