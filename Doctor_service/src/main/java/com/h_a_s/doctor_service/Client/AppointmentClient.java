package com.h_a_s.doctor_service.Client;

import com.h_a_s.doctor_service.Configurations.FeignConfig;
import com.h_a_s.doctor_service.Dto.Client.AppointmentClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "appointment-service", url = "${appointment.service.url}", configuration = FeignConfig.class)
public interface AppointmentClient {
    @GetMapping("/api/v1/appointments/{id}")
    AppointmentClientDto getAppointmentById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/appointments/doctor/{doctorId}/booked")
    List<LocalDateTime> getBookedStartTimes(
            @PathVariable("doctorId") Long doctorId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    );

    @PatchMapping("/api/v1/appointments/doctor/{doctorId}/cancel-scheduled")
    void cancelScheduledAppointmentsByDoctor(@PathVariable("doctorId") Long doctorId);
}

