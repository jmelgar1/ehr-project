package com.ehr.patient.service;

import com.ehr.patient.constant.ExceptionMessages;
import com.ehr.patient.dto.*;
import com.ehr.patient.exception.DuplicateResourceException;
import com.ehr.patient.exception.ResourceNotFoundException;
import com.ehr.patient.model.Diagnosis;
import com.ehr.patient.model.Medication;
import com.ehr.patient.model.Patient;
import com.ehr.patient.model.enums.PatientStatus;
import com.ehr.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PatientResponse createPatient(CreatePatientRequest request) {
        if (request.email() != null && patientRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateResourceException(ExceptionMessages.PATIENT_EMAIL_EXISTS);
        }

        Patient patient = Patient.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .city(request.city())
                .state(request.state())
                .zipCode(request.zipCode())
                .emergencyContactName(request.emergencyContactName())
                .emergencyContactPhone(request.emergencyContactPhone())
                .status(PatientStatus.ACTIVE)
                .build();

        Patient saved = patientRepository.save(patient);
        return toPatientResponse(saved);
    }

    public PatientResponse getPatient(UUID id) {
        Patient patient = patientRepository.findByIdWithDiagnosesAndMedications(id)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND));
        return toPatientResponse(patient);
    }

    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAllWithDiagnosesAndMedications().stream()
                .map(this::toPatientResponse)
                .toList();
    }

    public PatientResponse updatePatient(UUID id, UpdatePatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND));

        if (request.email() != null && !request.email().equals(patient.getEmail())) {
            if (patientRepository.findByEmail(request.email()).isPresent()) {
                throw new DuplicateResourceException(ExceptionMessages.PATIENT_EMAIL_EXISTS);
            }
        }

        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setEmail(request.email());
        patient.setPhone(request.phone());
        patient.setAddress(request.address());
        patient.setCity(request.city());
        patient.setState(request.state());
        patient.setZipCode(request.zipCode());
        patient.setEmergencyContactName(request.emergencyContactName());
        patient.setEmergencyContactPhone(request.emergencyContactPhone());
        patient.setStatus(request.status());

        Patient saved = patientRepository.save(patient);
        return toPatientResponse(saved);
    }

    private PatientResponse toPatientResponse(Patient patient) {
        List<DiagnosisResponse> diagnoses = patient.getDiagnoses().stream()
                .map(this::toDiagnosisResponse)
                .toList();

        List<MedicationResponse> medications = patient.getMedications().stream()
                .map(this::toMedicationResponse)
                .toList();

        return new PatientResponse(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getAddress(),
                patient.getCity(),
                patient.getState(),
                patient.getZipCode(),
                patient.getEmergencyContactName(),
                patient.getEmergencyContactPhone(),
                patient.getStatus(),
                patient.getCreatedAt(),
                patient.getUpdatedAt(),
                diagnoses,
                medications
        );
    }

    private DiagnosisResponse toDiagnosisResponse(Diagnosis diagnosis) {
        return new DiagnosisResponse(
                diagnosis.getId(),
                diagnosis.getPatient().getId(),
                diagnosis.getIcdCode(),
                diagnosis.getDescription(),
                diagnosis.getDiagnosisDate(),
                diagnosis.getStatus(),
                diagnosis.getCreatedAt(),
                diagnosis.getUpdatedAt()
        );
    }

    private MedicationResponse toMedicationResponse(Medication medication) {
        return new MedicationResponse(
                medication.getId(),
                medication.getPatient().getId(),
                medication.getName(),
                medication.getDosage(),
                medication.getFrequency(),
                medication.getStatus(),
                medication.getStartDate(),
                medication.getEndDate(),
                medication.isContraindicated(),
                medication.getWashoutDays(),
                medication.getWashoutNotes(),
                medication.getCreatedAt(),
                medication.getUpdatedAt()
        );
    }
}
