package com.ehr.patient.repository;

import com.ehr.patient.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, UUID> {

    List<Medication> findByPatientId(UUID patientId);

    List<Medication> findByPatientIdAndContraindicatedTrue(UUID patientId);
}
