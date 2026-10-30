package com.h_a_s.doctor_service.Dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsultationNoteCreateRequestDto {
    @NotNull(message = "Appointment ID is required!")
    @Positive(message = "Appointment ID must be a positive number.")
    private Long appointmentId;

    @NotNull(message = "Doctor ID is required!")
    @Positive(message = "Doctor ID must be a positive number.")
    private Long doctorId;

    @NotNull(message = "Patient ID is required!")
    @Positive(message = "Patient ID must be a positive number.")
    private Long patientId;

    @NotBlank(message = "Reason for visit is required!")
    private String reason;

    @NotBlank(message = "Diagnosis is required!")
    private String diagnosis;

    @Valid
    private List<PrescriptionDto> prescriptions;

    private String notes;
}
