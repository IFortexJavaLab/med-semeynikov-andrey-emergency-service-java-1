package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.emergency.EmergencyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergencyAssignmentRepository extends JpaRepository<EmergencyAssignment, Long> {

}
