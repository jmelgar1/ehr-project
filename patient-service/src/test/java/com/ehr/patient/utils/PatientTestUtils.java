package com.ehr.patient.utils;

import com.ehr.patient.dto.CreatePatientRequest;
import com.ehr.patient.dto.PatientResponse;
import com.ehr.patient.dto.UpdatePatientRequest;
import com.ehr.patient.model.Patient;
import com.ehr.patient.model.enums.PatientStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

public final class PatientTestUtils {

    private PatientTestUtils() {}

    public static CreatePatientRequest createPatientRequest() {
        return createPatientRequest(
                "John",
                "Doe",
                "john.doe@example.com"
        );
    }

    public static CreatePatientRequest createPatientRequest(String firstName, String lastName, String email) {
        return new CreatePatientRequest(
                firstName,
                lastName,
                LocalDate.of(1985, 3, 15),
                "Male",
                email,
                "555-123-4567",
                "123 Main St",
                "Springfield",
                "IL",
                "62701",
                "Jane Doe",
                "555-987-6543"
        );
    }

    public static UpdatePatientRequest updatePatientRequest() {
        return updatePatientRequest(
                "John",
                "Doe",
                "john.updated@example.com"
        );
    }

    public static UpdatePatientRequest updatePatientRequest(String firstName, String lastName, String email) {
        return new UpdatePatientRequest(
                firstName,
                lastName,
                LocalDate.of(1985, 3, 15),
                "Male",
                email,
                "555-111-2222",
                "456 Oak Ave",
                "Springfield",
                "IL",
                "62702",
                "Jane Doe",
                "555-987-6543",
                PatientStatus.ACTIVE
        );
    }

    public static Patient patient() {
        return patient(
                "John",
                "Doe",
                "john.doe@example.com"
        );
    }

    public static Patient patient(String firstName, String lastName, String email) {
        return Patient.builder()
                .id(UUID.randomUUID())
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .gender("Male")
                .email(email)
                .phone("555-123-4567")
                .address("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62701")
                .emergencyContactName("Jane Doe")
                .emergencyContactPhone("555-987-6543")
                .status(PatientStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .diagnoses(new HashSet<>())
                .medications(new HashSet<>())
                .build();
    }

    public static Patient patientWithId(UUID id) {
        Patient patient = patient();
        patient.setId(id);
        return patient;
    }

    public static PatientResponse patientResponse() {
        return patientResponse(UUID.randomUUID());
    }

    public static PatientResponse patientResponse(UUID id) {
        return patientResponse(
                id,
                "John",
                "Doe",
                "john.doe@example.com"
        );
    }

    public static PatientResponse patientResponse(UUID id, String firstName, String lastName, String email) {
        return new PatientResponse(
                id,
                firstName,
                lastName,
                LocalDate.of(1985, 3, 15),
                "Male",
                email,
                "555-123-4567",
                "123 Main St",
                "Springfield",
                "IL",
                "62701",
                "Jane Doe",
                "555-987-6543",
                PatientStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }
}
