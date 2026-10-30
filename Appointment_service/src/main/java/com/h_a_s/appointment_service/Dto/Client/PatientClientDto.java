package com.h_a_s.appointment_service.Dto.Client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientClientDto {
    private Long id;
    private String email;
    private String name;
    private LocalDate dob;
    private String contact;
    private String address;
    private boolean active;
}
