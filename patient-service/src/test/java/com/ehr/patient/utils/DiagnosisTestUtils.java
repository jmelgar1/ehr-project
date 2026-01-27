package com.ehr.patient.utils;

import com.ehr.patient.dto.CreateDiagnosisRequest;
import com.ehr.patient.dto.DiagnosisResponse;
import com.ehr.patient.model.Diagnosis;
import com.ehr.patient.model.Patient;
import com.ehr.patient.model.enums.DiagnosisStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public final class DiagnosisTestUtils {

    private DiagnosisTestUtils() {}

    public static CreateDiagnosisRequest createDiagnosisRequest() {
        return createDiagnosisRequest(
                "F43.10",
                "Post-traumatic stress disorder"
        );
    }

    public static CreateDiagnosisRequest createDiagnosisRequest(String icdCode, String description) {
        return new CreateDiagnosisRequest(
                icdCode,
                description,
                LocalDate.of(2024, 1, 15),
                DiagnosisStatus.ACTIVE
        );
    }

    public static Diagnosis diagnosis(Patient patient) {
        return diagnosis(
                patient,
                "F43.10",
                "Post-traumatic stress disorder"
        );
    }

    public static Diagnosis diagnosis(Patient patient, String icdCode, String description) {
        return Diagnosis.builder()
                .id(UUID.randomUUID())
                .patient(patient)
                .icdCode(icdCode)
                .description(description)
                .diagnosisDate(LocalDate.of(2024, 1, 15))
                .status(DiagnosisStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static DiagnosisResponse diagnosisResponse(UUID patientId) {
        return diagnosisResponse(
                UUID.randomUUID(),
                patientId
        );
    }

    public static DiagnosisResponse diagnosisResponse(UUID id, UUID patientId) {
        return new DiagnosisResponse(
                id,
                patientId,
                "F43.10",
                "Post-traumatic stress disorder",
                LocalDate.of(2024, 1, 15),
                DiagnosisStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
