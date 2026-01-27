package com.ehr.patient.controller;

import com.ehr.patient.constant.ApiPaths;
import com.ehr.patient.constant.ExceptionMessages;
import com.ehr.patient.exception.GlobalExceptionHandler;
import com.ehr.patient.exception.ResourceNotFoundException;
import com.ehr.patient.service.MedicationService;

import static com.ehr.patient.utils.MedicationTestUtils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicationController.class)
@Import(GlobalExceptionHandler.class)
class MedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MedicationService medicationService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void givenValidRequest_whenAddMedication_thenReturns201() throws Exception {
        var patientId = UUID.randomUUID();
        var request = createMedicationRequest();
        var response = medicationResponse(patientId);
        when(medicationService.addMedication(eq(patientId), any())).thenReturn(response);

        mockMvc.perform(post(ApiPaths.MEDICATIONS_API_PATH, patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.name").value("Sertraline"))
                .andExpect(jsonPath("$.dosage").value("50mg"));
    }

    @Test
    void givenNonExistentPatient_whenAddMedication_thenReturns404() throws Exception {
        var patientId = UUID.randomUUID();
        var request = createMedicationRequest();
        when(medicationService.addMedication(eq(patientId), any())).thenThrow(
                new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND)
        );

        mockMvc.perform(post(ApiPaths.MEDICATIONS_API_PATH, patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.PATIENT_NOT_FOUND));
    }

    @Test
    void givenPatientWithMedications_whenGetMedications_thenReturns200WithList() throws Exception {
        var patientId = UUID.randomUUID();
        var medication1 = medicationResponse(
                UUID.randomUUID(),
                patientId,
                false
        );
        var medication2 = medicationResponse(
                UUID.randomUUID(),
                patientId,
                true
        );
        when(medicationService.getMedications(patientId)).thenReturn(List.of(medication1, medication2));

        mockMvc.perform(get(ApiPaths.MEDICATIONS_API_PATH, patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void givenPatientWithNoMedications_whenGetMedications_thenReturns200WithEmptyList() throws Exception {
        var patientId = UUID.randomUUID();
        when(medicationService.getMedications(patientId)).thenReturn(List.of());

        mockMvc.perform(get(ApiPaths.MEDICATIONS_API_PATH, patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void givenNonExistentPatient_whenGetMedications_thenReturns404() throws Exception {
        var patientId = UUID.randomUUID();
        when(medicationService.getMedications(patientId)).thenThrow(
                new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND)
        );

        mockMvc.perform(get(ApiPaths.MEDICATIONS_API_PATH, patientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.PATIENT_NOT_FOUND));
    }

    @Test
    void givenValidRequest_whenUpdateMedication_thenReturns200() throws Exception {
        var patientId = UUID.randomUUID();
        var medicationId = UUID.randomUUID();
        var request = updateMedicationRequest();
        var response = medicationResponse(
                medicationId,
                patientId,
                false
        );
        when(medicationService.updateMedication(eq(patientId), eq(medicationId), any())).thenReturn(response);

        mockMvc.perform(put(ApiPaths.MEDICATIONS_API_PATH + "/{medicationId}", patientId, medicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(medicationId.toString()))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()));
    }

    @Test
    void givenNonExistentPatient_whenUpdateMedication_thenReturns404() throws Exception {
        var patientId = UUID.randomUUID();
        var medicationId = UUID.randomUUID();
        var request = updateMedicationRequest();
        when(medicationService.updateMedication(eq(patientId), eq(medicationId), any())).thenThrow(
                new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND)
        );

        mockMvc.perform(put(ApiPaths.MEDICATIONS_API_PATH + "/{medicationId}", patientId, medicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.PATIENT_NOT_FOUND));
    }

    @Test
    void givenNonExistentMedication_whenUpdateMedication_thenReturns404() throws Exception {
        var patientId = UUID.randomUUID();
        var medicationId = UUID.randomUUID();
        var request = updateMedicationRequest();
        when(medicationService.updateMedication(eq(patientId), eq(medicationId), any())).thenThrow(
                new ResourceNotFoundException(ExceptionMessages.MEDICATION_NOT_FOUND)
        );

        mockMvc.perform(put(ApiPaths.MEDICATIONS_API_PATH + "/{medicationId}", patientId, medicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.MEDICATION_NOT_FOUND));
    }

    @Test
    void givenPatientWithContraindicatedMedications_whenGetContraindications_thenReturns200() throws Exception {
        var patientId = UUID.randomUUID();
        var contraindicatedMed = medicationResponse(
                UUID.randomUUID(),
                patientId,
                true
        );
        when(medicationService.getContraindications(patientId)).thenReturn(List.of(contraindicatedMed));

        mockMvc.perform(get(ApiPaths.MEDICATIONS_API_PATH + "/contraindicated", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].contraindicated").value(true));
    }

    @Test
    void givenPatientWithNoContraindications_whenGetContraindications_thenReturns200WithEmptyList() throws Exception {
        var patientId = UUID.randomUUID();
        when(medicationService.getContraindications(patientId)).thenReturn(List.of());

        mockMvc.perform(get(ApiPaths.MEDICATIONS_API_PATH + "/contraindicated", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void givenNonExistentPatient_whenGetContraindications_thenReturns404() throws Exception {
        var patientId = UUID.randomUUID();
        when(medicationService.getContraindications(patientId)).thenThrow(
                new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND)
        );

        mockMvc.perform(get(ApiPaths.MEDICATIONS_API_PATH + "/contraindicated", patientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.PATIENT_NOT_FOUND));
    }
}
