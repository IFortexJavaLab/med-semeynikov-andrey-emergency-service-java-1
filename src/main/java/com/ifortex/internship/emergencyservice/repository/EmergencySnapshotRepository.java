package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencySnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmergencySnapshotRepository extends MongoRepository<EmergencySnapshot, String> {

    Optional<EmergencySnapshot> findByClientIdAndStatus(UUID clientId, EmergencyStatus status);

    Optional<EmergencySnapshot> findByParamedicIdAndStatus(UUID paramedicId, EmergencyStatus status);
}
