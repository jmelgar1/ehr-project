package com.ehr.patient.controller;

import com.ehr.patient.constant.ApiPaths;
import com.ehr.patient.constant.ExceptionMessages;
import com.ehr.patient.exception.DuplicateResourceException;
import com.ehr.patient.exception.GlobalExceptionHandler;
import com.ehr.patient.exception.ResourceNotFoundException;
import com.ehr.patient.service.PatientService;

import static com.ehr.patient.utils.PatientTestUtils.*;
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

@WebMvcTest(PatientController.class)
@Import(GlobalExceptionHandler.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void givenValidRequest_whenCreatePatient_thenReturns201() throws Exception {
        var request = createPatientRequest();
        var response = patientResponse();
        when(patientService.createPatient(any())).thenReturn(response);

        mockMvc.perform(post(ApiPaths.PATIENTS_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void givenDuplicateEmail_whenCreatePatient_thenReturns409() throws Exception {
        var request = createPatientRequest();
        when(patientService.createPatient(any())).thenThrow(
                new DuplicateResourceException(ExceptionMessages.PATIENT_EMAIL_EXISTS)
        );

        mockMvc.perform(post(ApiPaths.PATIENTS_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.PATIENT_EMAIL_EXISTS));
    }

    @Test
    void givenPatientsExist_whenGetAllPatients_thenReturns200WithList() throws Exception {
        var patient1 = patientResponse(
                UUID.randomUUID(),
                "John",
                "Doe",
                "john@example.com"
        );
        var patient2 = patientResponse(
                UUID.randomUUID(),
                "Jane",
                "Smith",
                "jane@example.com"
        );
        when(patientService.getAllPatients()).thenReturn(List.of(patient1, patient2));

        mockMvc.perform(get(ApiPaths.PATIENTS_API_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }

    @Test
    void givenNoPatientsExist_whenGetAllPatients_thenReturns200WithEmptyList() throws Exception {
        when(patientService.getAllPatients()).thenReturn(List.of());

        mockMvc.perform(get(ApiPaths.PATIENTS_API_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void givenExistingPatientId_whenGetPatient_thenReturns200() throws Exception {
        var patientId = UUID.randomUUID();
        var response = patientResponse(patientId);
        when(patientService.getPatient(patientId)).thenReturn(response);

        mockMvc.perform(get(ApiPaths.PATIENTS_API_PATH + "/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void givenNonExistentPatientId_whenGetPatient_thenReturns404() throws Exception {
        var patientId = UUID.randomUUID();
        when(patientService.getPatient(patientId)).thenThrow(
                new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND)
        );

        mockMvc.perform(get(ApiPaths.PATIENTS_API_PATH + "/{id}", patientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.PATIENT_NOT_FOUND));
    }

    @Test
    void givenValidRequest_whenUpdatePatient_thenReturns200() throws Exception {
        var patientId = UUID.randomUUID();
        var request = updatePatientRequest();
        var response = patientResponse(
                patientId,
                "John",
                "Doe",
                "john.updated@example.com"
        );
        when(patientService.updatePatient(eq(patientId), any())).thenReturn(response);

        mockMvc.perform(put(ApiPaths.PATIENTS_API_PATH + "/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));
    }

    @Test
    void givenNonExistentPatient_whenUpdatePatient_thenReturns404() throws Exception {
        var patientId = UUID.randomUUID();
        var request = updatePatientRequest();
        when(patientService.updatePatient(eq(patientId), any())).thenThrow(
                new ResourceNotFoundException(ExceptionMessages.PATIENT_NOT_FOUND)
        );

        mockMvc.perform(put(ApiPaths.PATIENTS_API_PATH + "/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.PATIENT_NOT_FOUND));
    }

    @Test
    void givenDuplicateEmailOnUpdate_whenUpdatePatient_thenReturns409() throws Exception {
        var patientId = UUID.randomUUID();
        var request = updatePatientRequest();
        when(patientService.updatePatient(eq(patientId), any())).thenThrow(
                new DuplicateResourceException(ExceptionMessages.PATIENT_EMAIL_EXISTS)
        );

        mockMvc.perform(put(ApiPaths.PATIENTS_API_PATH + "/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.PATIENT_EMAIL_EXISTS));
    }
}
