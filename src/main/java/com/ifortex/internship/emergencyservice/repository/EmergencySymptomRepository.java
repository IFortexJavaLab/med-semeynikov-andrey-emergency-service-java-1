package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.emergency.EmergencySymptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergencySymptomRepository extends JpaRepository<EmergencySymptom, Long> {
}
