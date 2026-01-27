package com.ehr.patient.service;

import com.ehr.patient.constant.ExceptionMessages;
import com.ehr.patient.dto.CreateMedicationRequest;
import com.ehr.patient.dto.MedicationResponse;
import com.ehr.patient.dto.UpdateMedicationRequest;
import com.ehr.patient.exception.ResourceNotFoundException;
import com.ehr.patient.model.Medication;
import com.ehr.patient.model.Patient;
import com.ehr.patient.repository.MedicationRepository;
import com.ehr.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final PatientRepository patientRepository;

    public MedicationService(MedicationRepository medicationRepository, PatientRepository patientRepository) {
        this.medicationRepository = medicationRepository;
        this.patientRepository = patientRepository;
    }

    public MedicationResponse addMedication(UUID patientId, CreateMedicationRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND));

        Medication medication = Medication.builder()
                .patient(patient)
                .name(request.name())
                .dosage(request.dosage())
                .frequency(request.frequency())
                .status(request.status())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .contraindicated(request.contraindicated())
                .washoutDays(request.washoutDays())
                .washoutNotes(request.washoutNotes())
                .build();

        Medication saved = medicationRepository.save(medication);
        return toMedicationResponse(saved);
    }

    public List<MedicationResponse> getMedications(UUID patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND);
        }
        return medicationRepository.findByPatientId(patientId).stream()
                .map(this::toMedicationResponse)
                .toList();
    }

    public MedicationResponse updateMedication(UUID patientId, UUID medicationId, UpdateMedicationRequest request) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND);
        }

        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessages.MEDICATION_NOT_FOUND));

        if (!medication.getPatient().getId().equals(patientId)) {
            throw new ResourceNotFoundException(ExceptionMessages.MEDICATION_NOT_FOUND_FOR_PATIENT);
        }

        medication.setName(request.name());
        medication.setDosage(request.dosage());
        medication.setFrequency(request.frequency());
        medication.setStatus(request.status());
        medication.setStartDate(request.startDate());
        medication.setEndDate(request.endDate());
        medication.setContraindicated(request.contraindicated());
        medication.setWashoutDays(request.washoutDays());
        medication.setWashoutNotes(request.washoutNotes());

        Medication saved = medicationRepository.save(medication);
        return toMedicationResponse(saved);
    }

    public List<MedicationResponse> getContraindications(UUID patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND);
        }
        return medicationRepository.findByPatientIdAndContraindicatedTrue(patientId).stream()
                .map(this::toMedicationResponse)
                .toList();
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
