package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.snapshot.EmergencySnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmergencySnapshotRepository extends MongoRepository<EmergencySnapshot, String> {
}
