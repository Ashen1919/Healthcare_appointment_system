package com.h_a_s.doctor_service.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrescriptionDto {
    @NotBlank(message = "Drug name is required!")
    private String drug;

    @NotBlank(message = "Dosage is required!")
    private String dosage;

    @Positive(message = "Number of days must be positive!")
    private int numberOfDays;
}
