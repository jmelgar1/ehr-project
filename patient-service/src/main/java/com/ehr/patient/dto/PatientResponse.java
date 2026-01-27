package com.ehr.patient.dto;

import com.ehr.patient.model.enums.PatientStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String email,
        String phone,
        String address,
        String city,
        String state,
        String zipCode,
        String emergencyContactName,
        String emergencyContactPhone,
        PatientStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<DiagnosisResponse> diagnoses,
        List<MedicationResponse> medications) {
}
