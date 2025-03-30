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
import java.util.Objects;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "paramedic_emergency_location")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParamedicEmergencyLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParamedicEmergencyLocation that = (ParamedicEmergencyLocation) o;
        return Objects.equals(id, that.id) && Objects.equals(emergency, that.emergency) && locationType == that.locationType
               && Objects.equals(latitude, that.latitude) && Objects.equals(longitude, that.longitude) && Objects.equals(
            timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(emergency);
        result = 31 * result + Objects.hashCode(locationType);
        result = 31 * result + Objects.hashCode(latitude);
        result = 31 * result + Objects.hashCode(longitude);
        result = 31 * result + Objects.hashCode(timestamp);
        return result;
    }
}
