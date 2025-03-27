package com.ifortex.internship.emergencyservice.model.emergency;

import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "emergency_location")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmergencyLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_id", nullable = false)
    Emergency emergency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    EmergencyLocationType locationType;

    @Column(nullable = false, precision = 10, scale = 6)
    BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 6)
    BigDecimal longitude;

    @UpdateTimestamp
    @Column(nullable = false)
    Instant timestamp;
}
