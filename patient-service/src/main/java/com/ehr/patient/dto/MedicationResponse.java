package com.ehr.patient.dto;

import com.ehr.patient.model.enums.MedicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MedicationResponse(
        UUID id,
        UUID patientId,
        String name,
        String dosage,
        String frequency,
        MedicationStatus status,
        LocalDate startDate,
        LocalDate endDate,
        boolean contraindicated,
        Integer washoutDays,
        String washoutNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
