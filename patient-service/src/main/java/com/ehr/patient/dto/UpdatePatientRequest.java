package com.ehr.patient.dto;

import com.ehr.patient.model.enums.PatientStatus;

import java.time.LocalDate;

public record UpdatePatientRequest(
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
        PatientStatus status) {
}
