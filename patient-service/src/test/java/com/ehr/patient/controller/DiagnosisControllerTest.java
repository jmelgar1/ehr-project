package com.ehr.patient.controller;

import com.ehr.patient.constant.ApiPaths;
import com.ehr.patient.constant.ExceptionMessages;
import com.ehr.patient.exception.GlobalExceptionHandler;
import com.ehr.patient.exception.ResourceNotFoundException;
import com.ehr.patient.service.DiagnosisService;

import static com.ehr.patient.utils.DiagnosisTestUtils.*;
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

@WebMvcTest(DiagnosisController.class)
@Import(GlobalExceptionHandler.class)
class DiagnosisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DiagnosisService diagnosisService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void givenValidRequest_whenAddDiagnosis_thenReturns201() throws Exception {
        var patientId = UUID.randomUUID();
        var request = createDiagnosisRequest();
        var response = diagnosisResponse(patientId);
        when(diagnosisService.addDiagnosis(eq(patientId), any())).thenReturn(response);

        mockMvc.perform(post(ApiPaths.DIAGNOSES_API_PATH, patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.icdCode").value("F43.10"))
                .andExpect(jsonPath("$.description").value("Post-traumatic stress disorder"));
    }

    @Test
    void givenNonExistentPatient_whenAddDiagnosis_thenReturns404() throws Exception {
        var patientId = UUID.randomUUID();
        var request = createDiagnosisRequest();
        when(diagnosisService.addDiagnosis(eq(patientId), any())).thenThrow(
                new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND)
        );

        mockMvc.perform(post(ApiPaths.DIAGNOSES_API_PATH, patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.PATIENT_NOT_FOUND));
    }

    @Test
    void givenPatientWithDiagnoses_whenGetDiagnoses_thenReturns200WithList() throws Exception {
        var patientId = UUID.randomUUID();
        var diagnosis1 = diagnosisResponse(
                UUID.randomUUID(),
                patientId
        );
        var diagnosis2 = diagnosisResponse(
                UUID.randomUUID(),
                patientId
        );
        when(diagnosisService.getDiagnoses(patientId)).thenReturn(List.of(diagnosis1, diagnosis2));

        mockMvc.perform(get(ApiPaths.DIAGNOSES_API_PATH, patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void givenPatientWithNoDiagnoses_whenGetDiagnoses_thenReturns200WithEmptyList() throws Exception {
        var patientId = UUID.randomUUID();
        when(diagnosisService.getDiagnoses(patientId)).thenReturn(List.of());

        mockMvc.perform(get(ApiPaths.DIAGNOSES_API_PATH, patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void givenNonExistentPatient_whenGetDiagnoses_thenReturns404() throws Exception {
        var patientId = UUID.randomUUID();
        when(diagnosisService.getDiagnoses(patientId)).thenThrow(
                new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND)
        );

        mockMvc.perform(get(ApiPaths.DIAGNOSES_API_PATH, patientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.PATIENT_NOT_FOUND));
    }

    @Test
    void givenValidRequest_whenUpdateDiagnosis_thenReturns200() throws Exception {
        var patientId = UUID.randomUUID();
        var diagnosisId = UUID.randomUUID();
        var request = createDiagnosisRequest(
                "F43.12",
                "Updated PTSD diagnosis"
        );
        var response = diagnosisResponse(
                diagnosisId,
                patientId
        );
        when(diagnosisService.updateDiagnosis(eq(patientId), eq(diagnosisId), any())).thenReturn(response);

        mockMvc.perform(put(ApiPaths.DIAGNOSES_API_PATH + "/{diagnosisId}", patientId, diagnosisId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(diagnosisId.toString()))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()));
    }

    @Test
    void givenNonExistentPatient_whenUpdateDiagnosis_thenReturns404() throws Exception {
        var patientId = UUID.randomUUID();
        var diagnosisId = UUID.randomUUID();
        var request = createDiagnosisRequest();
        when(diagnosisService.updateDiagnosis(eq(patientId), eq(diagnosisId), any())).thenThrow(
                new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND)
        );

        mockMvc.perform(put(ApiPaths.DIAGNOSES_API_PATH + "/{diagnosisId}", patientId, diagnosisId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.PATIENT_NOT_FOUND));
    }

    @Test
    void givenNonExistentDiagnosis_whenUpdateDiagnosis_thenReturns404() throws Exception {
        var patientId = UUID.randomUUID();
        var diagnosisId = UUID.randomUUID();
        var request = createDiagnosisRequest();
        when(diagnosisService.updateDiagnosis(eq(patientId), eq(diagnosisId), any())).thenThrow(
                new ResourceNotFoundException(ExceptionMessages.DIAGNOSIS_NOT_FOUND)
        );

        mockMvc.perform(put(ApiPaths.DIAGNOSES_API_PATH + "/{diagnosisId}", patientId, diagnosisId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.DIAGNOSIS_NOT_FOUND));
    }
}
