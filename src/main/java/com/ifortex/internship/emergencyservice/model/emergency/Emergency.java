package com.ifortex.internship.emergencyservice.model.emergency;

import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "emergency")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Emergency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    UUID clientId;

    UUID paramedicId;

    @Enumerated(EnumType.STRING)
    EmergencyStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolution_id")
    EmergencyResolutionEntity resolution;

    @Column(columnDefinition = "TEXT")
    String resolutionExplanation;

    @Column(nullable = false, precision = 10, scale = 6)
    BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 6)
    BigDecimal longitude;

    @OneToMany(mappedBy = "emergency", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ParamedicEmergencyLocation> paramedicLocations = new ArrayList<>();

    @OneToMany(mappedBy = "emergency", cascade = CascadeType.ALL, orphanRemoval = true)
    List<EmergencyAssignment> assignments = new ArrayList<>();

    @OneToOne(mappedBy = "emergency", cascade = CascadeType.ALL, orphanRemoval = true)
    EmergencyFeedback feedback;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    Instant updatedAt;
}
