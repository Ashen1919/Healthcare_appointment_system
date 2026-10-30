package com.h_a_s.appointment_service.Client;

import com.h_a_s.appointment_service.Configurations.FeignConfig;
import com.h_a_s.appointment_service.Dto.Client.PatientClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "patient-client", url = "${patient.service.url}", configuration = FeignConfig.class)
public interface PatientClient {

    // Get patient details by ID
    @GetMapping("/api/v1/patients/{id}")
    PatientClientDto getPatientById(@PathVariable("id") Long id);
}
