package com.ehr.patient.model;

import com.ehr.patient.model.enums.MedicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "MEDICATIONS")
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PATIENT_ID", nullable = false)
    private Patient patient;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "DOSAGE")
    private String dosage;

    @Column(name = "FREQUENCY")
    private String frequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private MedicationStatus status;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Builder.Default
    @Column(name = "CONTRAINDICATED")
    private boolean contraindicated = false;

    @Column(name = "WASHOUT_DAYS")
    private Integer washoutDays;

    @Column(name = "WASHOUT_NOTES")
    private String washoutNotes;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
