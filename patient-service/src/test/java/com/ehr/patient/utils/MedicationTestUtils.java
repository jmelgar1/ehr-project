package com.ehr.patient.utils;

import com.ehr.patient.dto.CreateMedicationRequest;
import com.ehr.patient.dto.MedicationResponse;
import com.ehr.patient.dto.UpdateMedicationRequest;
import com.ehr.patient.model.Medication;
import com.ehr.patient.model.Patient;
import com.ehr.patient.model.enums.MedicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public final class MedicationTestUtils {

    private MedicationTestUtils() {}

    public static CreateMedicationRequest createMedicationRequest() {
        return createMedicationRequest(
                "Sertraline",
                "50mg",
                false
        );
    }

    public static CreateMedicationRequest createMedicationRequest(String name, String dosage, boolean contraindicated) {
        return new CreateMedicationRequest(
                name,
                dosage,
                "Once daily",
                MedicationStatus.ACTIVE,
                LocalDate.of(2024, 1, 20),
                null,
                contraindicated,
                14,
                "Taper gradually if discontinuing"
        );
    }

    public static UpdateMedicationRequest updateMedicationRequest() {
        return updateMedicationRequest(
                "Sertraline",
                "100mg",
                false
        );
    }

    public static UpdateMedicationRequest updateMedicationRequest(String name, String dosage, boolean contraindicated) {
        return new UpdateMedicationRequest(
                name,
                dosage,
                "Once daily",
                MedicationStatus.ACTIVE,
                LocalDate.of(2024, 1, 20),
                null,
                contraindicated,
                14,
                "Increased dosage"
        );
    }

    public static Medication medication(Patient patient) {
        return medication(
                patient,
                "Sertraline",
                "50mg",
                false
        );
    }

    public static Medication medication(Patient patient, String name, String dosage, boolean contraindicated) {
        return Medication.builder()
                .id(UUID.randomUUID())
                .patient(patient)
                .name(name)
                .dosage(dosage)
                .frequency("Once daily")
                .status(MedicationStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 1, 20))
                .endDate(null)
                .contraindicated(contraindicated)
                .washoutDays(14)
                .washoutNotes("Taper gradually if discontinuing")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static MedicationResponse medicationResponse(UUID patientId) {
        return medicationResponse(
                UUID.randomUUID(),
                patientId,
                false
        );
    }

    public static MedicationResponse medicationResponse(UUID id, UUID patientId, boolean contraindicated) {
        return new MedicationResponse(
                id,
                patientId,
                "Sertraline",
                "50mg",
                "Once daily",
                MedicationStatus.ACTIVE,
                LocalDate.of(2024, 1, 20),
                null,
                contraindicated,
                14,
                "Taper gradually if discontinuing",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
