package com.h_a_s.doctor_service.Repository;

import com.h_a_s.doctor_service.Model.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepo extends MongoRepository<Doctor, Long> {
    boolean existsByEmail(String email);
}
