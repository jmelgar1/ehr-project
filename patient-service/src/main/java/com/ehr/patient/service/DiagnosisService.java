package com.ehr.patient.service;

import com.ehr.patient.constant.ExceptionMessages;
import com.ehr.patient.dto.CreateDiagnosisRequest;
import com.ehr.patient.dto.DiagnosisResponse;
import com.ehr.patient.exception.ResourceNotFoundException;
import com.ehr.patient.model.Diagnosis;
import com.ehr.patient.model.Patient;
import com.ehr.patient.repository.DiagnosisRepository;
import com.ehr.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final PatientRepository patientRepository;

    public DiagnosisService(DiagnosisRepository diagnosisRepository, PatientRepository patientRepository) {
        this.diagnosisRepository = diagnosisRepository;
        this.patientRepository = patientRepository;
    }

    public DiagnosisResponse addDiagnosis(UUID patientId, CreateDiagnosisRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND));

        Diagnosis diagnosis = Diagnosis.builder()
                .patient(patient)
                .icdCode(request.icdCode())
                .description(request.description())
                .diagnosisDate(request.diagnosisDate())
                .status(request.status())
                .build();

        Diagnosis saved = diagnosisRepository.save(diagnosis);
        return toDiagnosisResponse(saved);
    }

    public List<DiagnosisResponse> getDiagnoses(UUID patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND);
        }
        return diagnosisRepository.findByPatientId(patientId).stream()
                .map(this::toDiagnosisResponse)
                .toList();
    }

    public DiagnosisResponse updateDiagnosis(UUID patientId, UUID diagnosisId, CreateDiagnosisRequest request) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND);
        }

        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessages.DIAGNOSIS_NOT_FOUND));

        if (!diagnosis.getPatient().getId().equals(patientId)) {
            throw new ResourceNotFoundException(ExceptionMessages.DIAGNOSIS_NOT_FOUND_FOR_PATIENT);
        }

        diagnosis.setIcdCode(request.icdCode());
        diagnosis.setDescription(request.description());
        diagnosis.setDiagnosisDate(request.diagnosisDate());
        diagnosis.setStatus(request.status());

        Diagnosis saved = diagnosisRepository.save(diagnosis);
        return toDiagnosisResponse(saved);
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
}
