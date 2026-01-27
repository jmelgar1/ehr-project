package com.ehr.patient.service;

import com.ehr.patient.constant.ExceptionMessages;
import com.ehr.patient.exception.ResourceNotFoundException;
import com.ehr.patient.model.Medication;
import com.ehr.patient.repository.MedicationRepository;
import com.ehr.patient.repository.PatientRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ehr.patient.utils.MedicationTestUtils.*;
import static com.ehr.patient.utils.PatientTestUtils.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private PatientRepository patientRepository;

    private MedicationService medicationService;

    @BeforeEach
    void setUp() {
        medicationService = new MedicationService(medicationRepository, patientRepository);
    }

    @Test
    void givenValidRequest_whenAddMedication_thenReturnsMedicationResponse() {
        var patientId = UUID.randomUUID();
        var existingPatient = patientWithId(patientId);
        var request = createMedicationRequest();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(medicationRepository.save(any(Medication.class))).thenAnswer(invocation -> {
            Medication saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        var response = medicationService.addMedication(patientId, request);

        assertThat(response.patientId()).isEqualTo(patientId);
        assertThat(response.name()).isEqualTo("Sertraline");
        assertThat(response.dosage()).isEqualTo("50mg");
        assertThat(response.contraindicated()).isFalse();
        verify(medicationRepository).save(any(Medication.class));
    }

    @Test
    void givenContraindicatedMedication_whenAddMedication_thenReturnsMedicationResponseWithContraindicated() {
        var patientId = UUID.randomUUID();
        var existingPatient = patientWithId(patientId);
        var request = createMedicationRequest(
                "MAO Inhibitor",
                "10mg",
                true
        );
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(medicationRepository.save(any(Medication.class))).thenAnswer(invocation -> {
            Medication saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        var response = medicationService.addMedication(patientId, request);

        assertThat(response.contraindicated()).isTrue();
        assertThat(response.name()).isEqualTo("MAO Inhibitor");
    }

    @Test
    void givenNonExistentPatient_whenAddMedication_thenThrowsResourceNotFoundException() {
        var patientId = UUID.randomUUID();
        var request = createMedicationRequest();
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicationService.addMedication(patientId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ExceptionMessages.PATIENT_NOT_FOUND);
    }

    @Test
    void givenPatientWithMedications_whenGetMedications_thenReturnsListOfMedicationResponses() {
        var patientId = UUID.randomUUID();
        var existingPatient = patientWithId(patientId);
        var medication1 = medication(existingPatient, "Sertraline", "50mg", false);
        var medication2 = medication(existingPatient, "Lorazepam", "1mg", true);
        when(patientRepository.existsById(patientId)).thenReturn(true);
        when(medicationRepository.findByPatientId(patientId)).thenReturn(List.of(medication1, medication2));

        var response = medicationService.getMedications(patientId);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).name()).isEqualTo("Sertraline");
        assertThat(response.get(1).name()).isEqualTo("Lorazepam");
    }

    @Test
    void givenPatientWithNoMedications_whenGetMedications_thenReturnsEmptyList() {
        var patientId = UUID.randomUUID();
        when(patientRepository.existsById(patientId)).thenReturn(true);
        when(medicationRepository.findByPatientId(patientId)).thenReturn(List.of());

        var response = medicationService.getMedications(patientId);

        assertThat(response).isEmpty();
    }

    @Test
    void givenNonExistentPatient_whenGetMedications_thenThrowsResourceNotFoundException() {
        var patientId = UUID.randomUUID();
        when(patientRepository.existsById(patientId)).thenReturn(false);

        assertThatThrownBy(() -> medicationService.getMedications(patientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ExceptionMessages.PATIENT_NOT_FOUND);
    }

    @Test
    void givenValidRequest_whenUpdateMedication_thenReturnsUpdatedMedicationResponse() {
        var patientId = UUID.randomUUID();
        var medicationId = UUID.randomUUID();
        var existingPatient = patientWithId(patientId);
        var existingMedication = medication(existingPatient);
        existingMedication.setId(medicationId);
        var request = updateMedicationRequest(
                "Sertraline",
                "100mg",
                false
        );
        when(patientRepository.existsById(patientId)).thenReturn(true);
        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(existingMedication));
        when(medicationRepository.save(any(Medication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = medicationService.updateMedication(patientId, medicationId, request);

        assertThat(response.dosage()).isEqualTo("100mg");
        verify(medicationRepository).save(any(Medication.class));
    }

    @Test
    void givenNonExistentPatient_whenUpdateMedication_thenThrowsResourceNotFoundException() {
        var patientId = UUID.randomUUID();
        var medicationId = UUID.randomUUID();
        var request = updateMedicationRequest();
        when(patientRepository.existsById(patientId)).thenReturn(false);

        assertThatThrownBy(() -> medicationService.updateMedication(patientId, medicationId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ExceptionMessages.PATIENT_NOT_FOUND);
    }

    @Test
    void givenNonExistentMedication_whenUpdateMedication_thenThrowsResourceNotFoundException() {
        var patientId = UUID.randomUUID();
        var medicationId = UUID.randomUUID();
        var request = updateMedicationRequest();
        when(patientRepository.existsById(patientId)).thenReturn(true);
        when(medicationRepository.findById(medicationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicationService.updateMedication(patientId, medicationId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ExceptionMessages.MEDICATION_NOT_FOUND);
    }

    @Test
    void givenMedicationBelongsToDifferentPatient_whenUpdateMedication_thenThrowsResourceNotFoundException() {
        var patientId = UUID.randomUUID();
        var differentPatientId = UUID.randomUUID();
        var medicationId = UUID.randomUUID();
        var differentPatient = patientWithId(differentPatientId);
        var existingMedication = medication(differentPatient);
        existingMedication.setId(medicationId);
        var request = updateMedicationRequest();
        when(patientRepository.existsById(patientId)).thenReturn(true);
        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(existingMedication));

        assertThatThrownBy(() -> medicationService.updateMedication(patientId, medicationId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ExceptionMessages.MEDICATION_NOT_FOUND_FOR_PATIENT);
    }

    @Test
    void givenPatientWithContraindicatedMedications_whenGetContraindications_thenReturnsOnlyContraindicated() {
        var patientId = UUID.randomUUID();
        var existingPatient = patientWithId(patientId);
        var contraindicatedMed = medication(existingPatient, "MAO Inhibitor", "10mg", true);
        when(patientRepository.existsById(patientId)).thenReturn(true);
        when(medicationRepository.findByPatientIdAndContraindicatedTrue(patientId))
                .thenReturn(List.of(contraindicatedMed));

        var response = medicationService.getContraindications(patientId);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).contraindicated()).isTrue();
        assertThat(response.get(0).name()).isEqualTo("MAO Inhibitor");
    }

    @Test
    void givenPatientWithNoContraindications_whenGetContraindications_thenReturnsEmptyList() {
        var patientId = UUID.randomUUID();
        when(patientRepository.existsById(patientId)).thenReturn(true);
        when(medicationRepository.findByPatientIdAndContraindicatedTrue(patientId)).thenReturn(List.of());

        var response = medicationService.getContraindications(patientId);

        assertThat(response).isEmpty();
    }

    @Test
    void givenNonExistentPatient_whenGetContraindications_thenThrowsResourceNotFoundException() {
        var patientId = UUID.randomUUID();
        when(patientRepository.existsById(patientId)).thenReturn(false);

        assertThatThrownBy(() -> medicationService.getContraindications(patientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ExceptionMessages.PATIENT_NOT_FOUND);
    }
}
