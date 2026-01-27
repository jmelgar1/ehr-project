package com.ehr.patient.dto;

import com.ehr.patient.model.enums.DiagnosisStatus;

import java.time.LocalDate;

public record CreateDiagnosisRequest(
        String icdCode,
        String description,
        LocalDate diagnosisDate,
        DiagnosisStatus status) {
}
