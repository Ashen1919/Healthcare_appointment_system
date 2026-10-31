package com.h_a_s.patient_service.Client;

import com.h_a_s.patient_service.Configurations.FeignConfig;
import com.h_a_s.patient_service.Dto.Client.AppointmentClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import com.h_a_s.patient_service.Dto.PageResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "appointment-service", url = "${appointment.service.url}", configuration = FeignConfig.class)
public interface AppointmentClient {
    @GetMapping("/api/v1/appointments/{id}")
    AppointmentClientDto getAppointmentById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/appointments/patient/{patientId}")
    PageResponseDto<AppointmentClientDto> getAppointmentsByPatient(
            @PathVariable("patientId") Long patientId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    @PatchMapping("/api/v1/appointments/patient/{patientId}/cancel-scheduled")
    void cancelScheduledAppointmentsByPatient(@PathVariable("patientId") Long patientId);
}

