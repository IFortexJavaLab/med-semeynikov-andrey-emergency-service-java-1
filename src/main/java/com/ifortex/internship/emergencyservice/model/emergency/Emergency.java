package com.ifortex.internship.emergencyservice.model.emergency;

import com.ifortex.internship.emergencyservice.model.constant.EmergencyResolution;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @Enumerated(EnumType.STRING)
    EmergencyResolution resolution;

    @Column(columnDefinition = "TEXT")
    String resolutionExplanation;

    @OneToMany(mappedBy = "emergency", cascade = CascadeType.ALL, orphanRemoval = true)
    List<EmergencyLocation> locations = new ArrayList<>();

    @OneToMany(mappedBy = "emergency", cascade = CascadeType.ALL, orphanRemoval = true)
    List<EmergencySymptom> symptoms = new ArrayList<>();

    @OneToMany(mappedBy = "emergency", cascade = CascadeType.ALL, orphanRemoval = true)
    List<EmergencyAssignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "emergency", cascade = CascadeType.ALL, orphanRemoval = true)
    List<EmergencyCancellation> cancellations = new ArrayList<>();

    @OneToOne(mappedBy = "emergency", cascade = CascadeType.ALL, orphanRemoval = true)
    EmergencyFeedback feedback;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    Instant updatedAt;
}
