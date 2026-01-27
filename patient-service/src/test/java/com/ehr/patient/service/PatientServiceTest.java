package com.ehr.patient.service;

import com.ehr.patient.constant.ExceptionMessages;
import com.ehr.patient.exception.DuplicateResourceException;
import com.ehr.patient.exception.ResourceNotFoundException;
import com.ehr.patient.model.Patient;
import com.ehr.patient.repository.PatientRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(patientRepository);
    }

    @Test
    void givenValidRequest_whenCreatePatient_thenReturnsPatientResponse() {
        var request = createPatientRequest();
        when(patientRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        var response = patientService.createPatient(request);

        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.email()).isEqualTo("john.doe@example.com");
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void givenExistingEmail_whenCreatePatient_thenThrowsDuplicateResourceException() {
        var request = createPatientRequest();
        var existingPatient = patient();
        when(patientRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(existingPatient));

        assertThatThrownBy(() -> patientService.createPatient(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(ExceptionMessages.PATIENT_EMAIL_EXISTS);
    }

    @Test
    void givenExistingPatientId_whenGetPatient_thenReturnsPatientResponse() {
        var patientId = UUID.randomUUID();
        var existingPatient = patientWithId(patientId);
        when(patientRepository.findByIdWithDiagnosesAndMedications(patientId)).thenReturn(Optional.of(existingPatient));

        var response = patientService.getPatient(patientId);

        assertThat(response.id()).isEqualTo(patientId);
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
    }

    @Test
    void givenNonExistentPatientId_whenGetPatient_thenThrowsResourceNotFoundException() {
        var patientId = UUID.randomUUID();
        when(patientRepository.findByIdWithDiagnosesAndMedications(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatient(patientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ExceptionMessages.PATIENT_NOT_FOUND);
    }

    @Test
    void givenPatientsExist_whenGetAllPatients_thenReturnsListOfPatientResponses() {
        var patient1 = patient(
                "John",
                "Doe",
                "john@example.com"
        );
        var patient2 = patient(
                "Jane",
                "Smith",
                "jane@example.com"
        );
        when(patientRepository.findAllWithDiagnosesAndMedications()).thenReturn(List.of(patient1, patient2));

        var response = patientService.getAllPatients();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).firstName()).isEqualTo("John");
        assertThat(response.get(1).firstName()).isEqualTo("Jane");
    }

    @Test
    void givenNoPatientsExist_whenGetAllPatients_thenReturnsEmptyList() {
        when(patientRepository.findAllWithDiagnosesAndMedications()).thenReturn(List.of());

        var response = patientService.getAllPatients();

        assertThat(response).isEmpty();
    }

    @Test
    void givenExistingPatient_whenUpdatePatient_thenReturnsUpdatedPatientResponse() {
        var patientId = UUID.randomUUID();
        var existingPatient = patientWithId(patientId);
        var request = updatePatientRequest();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = patientService.updatePatient(patientId, request);

        assertThat(response.email()).isEqualTo("john.updated@example.com");
        assertThat(response.address()).isEqualTo("456 Oak Ave");
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void givenNonExistentPatient_whenUpdatePatient_thenThrowsResourceNotFoundException() {
        var patientId = UUID.randomUUID();
        var request = updatePatientRequest();
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.updatePatient(patientId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ExceptionMessages.PATIENT_NOT_FOUND);
    }

    @Test
    void givenEmailAlreadyUsedByAnotherPatient_whenUpdatePatient_thenThrowsDuplicateResourceException() {
        var patientId = UUID.randomUUID();
        var existingPatient = patientWithId(patientId);
        existingPatient.setEmail("original@example.com");
        var anotherPatient = patient(
                "Other",
                "Person",
                "john.updated@example.com"
        );
        var request = updatePatientRequest();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(patientRepository.findByEmail("john.updated@example.com")).thenReturn(Optional.of(anotherPatient));

        assertThatThrownBy(() -> patientService.updatePatient(patientId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(ExceptionMessages.PATIENT_EMAIL_EXISTS);
    }
}
