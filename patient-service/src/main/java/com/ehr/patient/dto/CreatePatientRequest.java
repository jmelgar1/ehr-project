package com.ehr.patient.dto;

import java.time.LocalDate;

public record CreatePatientRequest(
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
        String emergencyContactPhone) {
}
