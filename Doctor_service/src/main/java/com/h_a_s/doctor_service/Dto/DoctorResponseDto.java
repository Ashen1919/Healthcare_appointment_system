package com.h_a_s.doctor_service.Dto;

import com.h_a_s.doctor_service.Model.Doctor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorResponseDto {
    private Long id;
    private String email;
    private String name;
    private String specialty;
    private String contact;
    private List<Doctor.ScheduleSlot> schedule;
    private List<Doctor.TimeOff> timeOff;
    private Instant createdAt;
    private Instant updatedAt;
}
