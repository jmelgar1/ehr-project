package com.ehr.patient.controller;

import com.ehr.patient.constant.ApiPaths;
import com.ehr.patient.dto.CreateDiagnosisRequest;
import com.ehr.patient.dto.DiagnosisResponse;
import com.ehr.patient.service.DiagnosisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.DIAGNOSES_API_PATH)
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping
    public ResponseEntity<DiagnosisResponse> addDiagnosis(@PathVariable UUID patientId,
                                                          @RequestBody CreateDiagnosisRequest request) {
        DiagnosisResponse response = diagnosisService.addDiagnosis(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DiagnosisResponse>> getDiagnoses(@PathVariable UUID patientId) {
        List<DiagnosisResponse> diagnoses = diagnosisService.getDiagnoses(patientId);
        return ResponseEntity.ok(diagnoses);
    }

    @PutMapping("/{diagnosisId}")
    public ResponseEntity<DiagnosisResponse> updateDiagnosis(@PathVariable UUID patientId,
                                                             @PathVariable UUID diagnosisId,
                                                             @RequestBody CreateDiagnosisRequest request) {
        DiagnosisResponse response = diagnosisService.updateDiagnosis(patientId, diagnosisId, request);
        return ResponseEntity.ok(response);
    }
}
