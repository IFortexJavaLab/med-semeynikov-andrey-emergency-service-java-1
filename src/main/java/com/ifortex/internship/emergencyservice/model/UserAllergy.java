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
@Table(name = "user_allergies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAllergy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allergy_id")
    Allergy allergy;

    @Column(name = "custom_allergy")
    String customAllergy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    Instant createdAt;

    public UserAllergy(UUID userId, Allergy allergy) {
        this.userId = userId;
        this.allergy = allergy;
    }

    public UserAllergy(UUID userId, String customAllergy) {
        this.customAllergy = customAllergy;
        this.userId = userId;
    }

    @PrePersist
    @PreUpdate
    private void validateAllergy() {
        if (allergy == null && (customAllergy == null || customAllergy.trim().isEmpty())) {
            throw new IllegalArgumentException("Either allergy_id or custom_allergy must be provided.");
        }
    }
}