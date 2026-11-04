package com.auth_service.auth_service.Client;

import com.auth_service.auth_service.Dto.DoctorUserCreateRequestDto.ScheduleSlotDto;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "doctor-client", url = "${doctor.service.url}")
public interface DoctorClient {

    @PostMapping("/api/v1/doctors")
    DoctorResponsePayload createDoctor(@RequestBody DoctorRequestPayload payload);

    @Data
    class DoctorRequestPayload {
        private String email;
        private String name;
        private String specialty;
        private String contact;
        private List<ScheduleSlotDto> schedule;
    }

    @Data
    class DoctorResponsePayload {
        private Long id;
        private String email;
        private String name;
        private String specialty;
        private String contact;
        private boolean active;
        private List<ScheduleSlotDto> schedule;
    }
}