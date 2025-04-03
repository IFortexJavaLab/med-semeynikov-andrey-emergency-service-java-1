package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, UUID> {

    boolean existsByClientIdAndStatus(UUID clientId, EmergencyStatus status);

    Optional<Emergency> findByClientIdAndStatus(UUID clientId, EmergencyStatus status);

    Optional<Emergency> findByParamedicIdAndStatus(UUID paramedicId, EmergencyStatus status);

    Optional<Emergency> findByIdAndClientId(UUID id, UUID clientId);

    Optional<Emergency> findByIdAndParamedicId(UUID id, UUID clientId);

    Page<Emergency> findByParamedicIdOrderByCreatedAtDesc(UUID paramedicId, Pageable pageable);

    Page<Emergency> findByClientIdOrderByCreatedAtDesc(UUID clientId, Pageable pageable);
}
