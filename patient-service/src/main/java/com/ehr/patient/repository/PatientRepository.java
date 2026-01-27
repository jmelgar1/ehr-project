package com.ehr.patient.repository;

import com.ehr.patient.model.Patient;
import com.ehr.patient.model.enums.PatientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.diagnoses LEFT JOIN FETCH p.medications")
    List<Patient> findAllWithDiagnosesAndMedications();

    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.diagnoses LEFT JOIN FETCH p.medications WHERE p.id = :id")
    Optional<Patient> findByIdWithDiagnosesAndMedications(UUID id);

    List<Patient> findByStatus(PatientStatus status);

    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    Optional<Patient> findByEmail(String email);
}
