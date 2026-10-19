package com.h_a_s.patient_service.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientResponseDto {
    private long Id;
    private String email;
    private String name;
    private LocalDate dob;
    private String contact;
    private String address;
    private boolean active;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
