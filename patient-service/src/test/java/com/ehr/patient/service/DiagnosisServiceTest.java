package com.ehr.patient.service;

import com.ehr.patient.constant.ExceptionMessages;
import com.ehr.patient.exception.ResourceNotFoundException;
import com.ehr.patient.model.Diagnosis;
import com.ehr.patient.repository.DiagnosisRepository;
import com.ehr.patient.repository.PatientRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ehr.patient.utils.DiagnosisTestUtils.*;
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
class DiagnosisServiceTest {

    @Mock
    private DiagnosisRepository diagnosisRepository;

    @Mock
    private PatientRepository patientRepository;

    private DiagnosisService diagnosisService;

    @BeforeEach
    void setUp() {
        diagnosisService = new DiagnosisService(diagnosisRepository, patientRepository);
    }

    @Test
    void givenValidRequest_whenAddDiagnosis_thenReturnsDiagnosisResponse() {
        var patientId = UUID.randomUUID();
        var existingPatient = patientWithId(patientId);
        var request = createDiagnosisRequest();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(diagnosisRepository.save(any(Diagnosis.class))).thenAnswer(invocation -> {
            Diagnosis saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        var response = diagnosisService.addDiagnosis(patientId, request);

        assertThat(response.patientId()).isEqualTo(patientId);
        assertThat(response.icdCode()).isEqualTo("F43.10");
        assertThat(response.description()).isEqualTo("Post-traumatic stress disorder");
        verify(diagnosisRepository).save(any(Diagnosis.class));
    }

    @Test
    void givenNonExistentPatient_whenAddDiagnosis_thenThrowsResourceNotFoundException() {
        var patientId = UUID.randomUUID();
        var request = createDiagnosisRequest();
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diagnosisService.addDiagnosis(patientId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ExceptionMessages.PATIENT_NOT_FOUND);
    }

    @Test
    void givenPatientWithDiagnoses_whenGetDiagnoses_thenReturnsListOfDiagnosisResponses() {
        var patientId = UUID.randomUUID();
        var existingPatient = patientWithId(patientId);
        var diagnosis1 = diagnosis(existingPatient, "F43.10", "PTSD");
        var diagnosis2 = diagnosis(existingPatient, "F32.1", "Major Depression");
        when(patientRepository.existsById(patientId)).thenReturn(true);
        when(diagnosisRepository.findByPatientId(patientId)).thenReturn(List.of(diagnosis1, diagnosis2));

        var response = diagnosisService.getDiagnoses(patientId);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).icdCode()).isEqualTo("F43.10");
        assertThat(response.get(1).icdCode()).isEqualTo("F32.1");
    }

    @Test
    void givenPatientWithNoDiagnoses_whenGetDiagnoses_thenReturnsEmptyList() {
        var patientId = UUID.randomUUID();
        when(patientRepository.existsById(patientId)).thenReturn(true);
        when(diagnosisRepository.findByPatientId(patientId)).thenReturn(List.of());

        var response = diagnosisService.getDiagnoses(patientId);

        assertThat(response).isEmpty();
    }

    @Test
    void givenNonExistentPatient_whenGetDiagnoses_thenThrowsResourceNotFoundException() {
        var patientId = UUID.randomUUID();
        when(patientRepository.existsById(patientId)).thenReturn(false);

        assertThatThrownBy(() -> diagnosisService.getDiagnoses(patientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ExceptionMessages.PATIENT_NOT_FOUND);
    }

    @Test
    void givenValidRequest_whenUpdateDiagnosis_thenReturnsUpdatedDiagnosisResponse() {
        var patientId = UUID.randomUUID();
        var diagnosisId = UUID.randomUUID();
        var existingPatient = patientWithId(patientId);
        var existingDiagnosis = diagnosis(existingPatient);
        existingDiagnosis.setId(diagnosisId);
        var request = createDiagnosisRequest(
                "F43.12",
                "Updated PTSD diagnosis"
        );
        when(patientRepository.existsById(patientId)).thenReturn(true);
        when(diagnosisRepository.findById(diagnosisId)).thenReturn(Optional.of(existingDiagnosis));
        when(diagnosisRepository.save(any(Diagnosis.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = diagnosisService.updateDiagnosis(patientId, diagnosisId, request);

        assertThat(response.icdCode()).isEqualTo("F43.12");
        assertThat(response.description()).isEqualTo("Updated PTSD diagnosis");
        verify(diagnosisRepository).save(any(Diagnosis.class));
    }

    @Test
    void givenNonExistentPatient_whenUpdateDiagnosis_thenThrowsResourceNotFoundException() {
        var patientId = UUID.randomUUID();
        var diagnosisId = UUID.randomUUID();
        var request = createDiagnosisRequest();
        when(patientRepository.existsById(patientId)).thenReturn(false);

        assertThatThrownBy(() -> diagnosisService.updateDiagnosis(patientId, diagnosisId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ExceptionMessages.PATIENT_NOT_FOUND);
    }

    @Test
    void givenNonExistentDiagnosis_whenUpdateDiagnosis_thenThrowsResourceNotFoundException() {
        var patientId = UUID.randomUUID();
        var diagnosisId = UUID.randomUUID();
        var request = createDiagnosisRequest();
        when(patientRepository.existsById(patientId)).thenReturn(true);
        when(diagnosisRepository.findById(diagnosisId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diagnosisService.updateDiagnosis(patientId, diagnosisId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ExceptionMessages.DIAGNOSIS_NOT_FOUND);
    }

    @Test
    void givenDiagnosisBelongsToDifferentPatient_whenUpdateDiagnosis_thenThrowsResourceNotFoundException() {
        var patientId = UUID.randomUUID();
        var differentPatientId = UUID.randomUUID();
        var diagnosisId = UUID.randomUUID();
        var differentPatient = patientWithId(differentPatientId);
        var existingDiagnosis = diagnosis(differentPatient);
        existingDiagnosis.setId(diagnosisId);
        var request = createDiagnosisRequest();
        when(patientRepository.existsById(patientId)).thenReturn(true);
        when(diagnosisRepository.findById(diagnosisId)).thenReturn(Optional.of(existingDiagnosis));

        assertThatThrownBy(() -> diagnosisService.updateDiagnosis(patientId, diagnosisId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ExceptionMessages.DIAGNOSIS_NOT_FOUND_FOR_PATIENT);
    }
}
