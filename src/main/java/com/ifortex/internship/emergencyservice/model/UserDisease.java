package com.ifortex.internship.emergencyservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_diseases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDisease {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id")
    Disease disease;

    @Column(name = "custom_disease")
    String customDisease;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @PrePersist
    @PreUpdate
    private void validateDisease() {
        if (disease == null && (customDisease == null || customDisease.trim().isEmpty())) {
            throw new IllegalArgumentException("Either disease_id or custom_disease must be provided.");
        }
    }

    public UserDisease(UUID userId, Disease disease) {
        this.userId = userId;
        this.disease = disease;
    }

    public UserDisease(UUID userId, String customDisease) {
        this.userId = userId;
        this.customDisease = customDisease;
    }
}
