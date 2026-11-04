package com.auth_service.auth_service.Client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.time.LocalDateTime;

@FeignClient(name = "patient-client", url = "${patient.service.url}")
public interface PatientClient {

    @PostMapping("/api/v1/patients")
    PatientResponsePayload createPatient(@RequestBody PatientRequestPayload payload);

    @Data
    class PatientRequestPayload {
        private String email;
        private String name;
        private LocalDate dob;
        private String contact;
        private String address;
    }

    @Data
    class PatientResponsePayload {
        private Long id;
        private String email;
        private String name;
        private LocalDate dob;
        private String contact;
        private String address;
        private boolean active;
        private LocalDateTime created_at;
        private LocalDateTime updated_at;
    }
}