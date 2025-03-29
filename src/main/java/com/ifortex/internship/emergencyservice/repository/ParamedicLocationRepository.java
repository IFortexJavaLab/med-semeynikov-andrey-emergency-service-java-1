package com.ifortex.internship.emergencyservice.repository;

import com.ifortex.internship.emergencyservice.model.ParamedicLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParamedicLocationRepository extends JpaRepository<ParamedicLocation, UUID> {

    @Query(value = """
        SELECT paramedic_id, latitude, longitude, updated_at
        FROM paramedic_location
        WHERE (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(latitude)) *
                cos(radians(longitude) - radians(:lon)) +
                sin(radians(:lat)) * sin(radians(latitude))
            )
        ) <= :radius
          AND paramedic_id NOT IN (
              SELECT paramedic_id FROM emergency WHERE status = 'ONGOING' AND paramedic_id IS NOT NULL
          )
        ORDER BY (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(latitude)) *
                cos(radians(longitude) - radians(:lon)) +
                sin(radians(:lat)) * sin(radians(latitude))
            )
        )
        LIMIT 1
        """, nativeQuery = true)
    Optional<ParamedicLocation> findNearestAvailableParamedicInRadius(
        @Param("lat") BigDecimal latitude,
        @Param("lon") BigDecimal longitude,
        @Param("radius") double radiusInKm
    );
}
