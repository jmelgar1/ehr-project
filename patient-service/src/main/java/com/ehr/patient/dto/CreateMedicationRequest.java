package com.ehr.patient.dto;

import com.ehr.patient.model.enums.MedicationStatus;

import java.time.LocalDate;

public record CreateMedicationRequest(
        String name,
        String dosage,
        String frequency,
        MedicationStatus status,
        LocalDate startDate,
        LocalDate endDate,
        boolean contraindicated,
        Integer washoutDays,
        String washoutNotes) {
}
