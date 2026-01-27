package com.ehr.patient.constant;

public final class ApiPaths {

    private ApiPaths() {}

    public static final String PATIENTS_API_PATH = "/api/patients";
    public static final String DIAGNOSES_API_PATH = "/api/patients/{patientId}/diagnoses";
    public static final String MEDICATIONS_API_PATH = "/api/patients/{patientId}/medications";
}
