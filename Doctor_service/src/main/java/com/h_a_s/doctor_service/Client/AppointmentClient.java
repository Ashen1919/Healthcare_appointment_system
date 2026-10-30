package com.h_a_s.doctor_service.Client;

import com.h_a_s.doctor_service.Configurations.FeignConfig;
import com.h_a_s.doctor_service.Dto.Client.AppointmentClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "appointment-service", url = "${appointment.service.url}", configuration = FeignConfig.class)
public interface AppointmentClient {
    @GetMapping("/api/v1/appointments/{id}")
    AppointmentClientDto getAppointmentById(@PathVariable("id") Long id);
}
