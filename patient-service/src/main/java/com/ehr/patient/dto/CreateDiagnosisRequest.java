package com.ehr.patient.dto;

import com.ehr.patient.model.enums.DiagnosisStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateDiagnosisRequest(
        @NotBlank String icdCode,
        String description,
        @NotNull LocalDate diagnosisDate,
        @NotNull DiagnosisStatus status) {
}
