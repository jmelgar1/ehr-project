package com.ehr.patient.controller;

import com.ehr.patient.constant.ApiPaths;
import com.ehr.patient.dto.CreateMedicationRequest;
import com.ehr.patient.dto.MedicationResponse;
import com.ehr.patient.dto.UpdateMedicationRequest;
import com.ehr.patient.service.MedicationService;
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
@RequestMapping(ApiPaths.MEDICATIONS_API_PATH)
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @PostMapping
    public ResponseEntity<MedicationResponse> addMedication(@PathVariable UUID patientId,
                                                            @RequestBody CreateMedicationRequest request) {
        MedicationResponse response = medicationService.addMedication(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MedicationResponse>> getMedications(@PathVariable UUID patientId) {
        List<MedicationResponse> medications = medicationService.getMedications(patientId);
        return ResponseEntity.ok(medications);
    }

    @PutMapping("/{medicationId}")
    public ResponseEntity<MedicationResponse> updateMedication(@PathVariable UUID patientId,
                                                               @PathVariable UUID medicationId,
                                                               @RequestBody UpdateMedicationRequest request) {
        MedicationResponse response = medicationService.updateMedication(patientId, medicationId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contraindicated")
    public ResponseEntity<List<MedicationResponse>> getContraindications(@PathVariable UUID patientId) {
        List<MedicationResponse> contraindications = medicationService.getContraindications(patientId);
        return ResponseEntity.ok(contraindications);
    }
}
