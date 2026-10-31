package com.h_a_s.appointment_service.Repository;

import com.h_a_s.appointment_service.Model.Appointment;
import com.h_a_s.appointment_service.Model.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointment, Long> {
    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);
    List<Appointment> findByDoctorIdAndStatusAndStartTimeBetween(Long doctorId, Status status, LocalDateTime from, LocalDateTime to);
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, Status status);
    List<Appointment> findByPatientIdAndStatus(Long patientId, Status status);
}
