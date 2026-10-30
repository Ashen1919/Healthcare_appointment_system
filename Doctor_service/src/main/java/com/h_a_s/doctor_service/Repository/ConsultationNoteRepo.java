package com.h_a_s.doctor_service.Repository;

import com.h_a_s.doctor_service.Model.ConsultationNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultationNoteRepo extends MongoRepository<ConsultationNote, Long> {
    boolean existsByAppointmentId(Long appointmentId);

    Optional<ConsultationNote> findByAppointmentId(Long appointmentId);

    Page<ConsultationNote> findByPatientId(Long patientId, Pageable pageable);

    Page<ConsultationNote> findByDoctorId(Long doctorId, Pageable pageable);
}
