package com.ehr.patient.dto;

import com.ehr.patient.model.enums.MedicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateMedicationRequest(
        @NotBlank String name,
        @NotBlank String dosage,
        @NotBlank String frequency,
        @NotNull MedicationStatus status,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        boolean contraindicated,
        Integer washoutDays,
        String washoutNotes) {
}
