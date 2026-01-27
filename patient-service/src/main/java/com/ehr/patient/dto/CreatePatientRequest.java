package com.ehr.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreatePatientRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull LocalDate dateOfBirth,
        @NotBlank String gender,
        @NotBlank String email,
        String phone,
        String address,
        String city,
        String state,
        String zipCode,
        String emergencyContactName,
        String emergencyContactPhone) {
}
