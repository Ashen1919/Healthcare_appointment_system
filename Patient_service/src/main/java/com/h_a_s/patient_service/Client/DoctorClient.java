package com.h_a_s.patient_service.Client;

import com.h_a_s.patient_service.Configurations.FeignConfig;
import com.h_a_s.patient_service.Dto.Client.ConsultationNoteClientDto;
import com.h_a_s.patient_service.Dto.PageResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "doctor-service", url = "${doctor.service.url}", configuration = FeignConfig.class)
public interface DoctorClient {

    @GetMapping("/api/v1/consultation-notes/patient/{patientId}")
    PageResponseDto<ConsultationNoteClientDto> getConsultationNotesByPatient(
            @PathVariable("patientId") Long patientId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );
}
