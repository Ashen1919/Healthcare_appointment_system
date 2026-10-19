package com.h_a_s.patient_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.h_a_s.patient_service.model.Patient;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepo extends JpaRepository<Patient, Long> { }
