package com.h_a_s.appointment_service.Client;

import com.h_a_s.appointment_service.Configurations.FeignConfig;
import com.h_a_s.appointment_service.Dto.Client.DoctorClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "doctor-client", url = "${doctor.service.url}", configuration = FeignConfig.class)
public interface DoctorClient {

    // get doctor details by ID
    @GetMapping("/api/v1/doctors/{id}")
    DoctorClientDto getDoctorById(@PathVariable("id") Long id);
}
