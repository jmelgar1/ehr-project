package com.ehr.patient.dto;

import com.ehr.patient.model.enums.DiagnosisStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DiagnosisResponse(
        UUID id,
        UUID patientId,
        String icdCode,
        String description,
        LocalDate diagnosisDate,
        DiagnosisStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
