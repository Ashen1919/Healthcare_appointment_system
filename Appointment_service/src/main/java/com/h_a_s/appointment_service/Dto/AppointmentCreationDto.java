package com.h_a_s.appointment_service.Dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentCreationDto {
    @NotNull(message = "Patient ID is required!")
    @Positive(message = "Patient ID must be a positive number.")
    private Long patientId;

    @NotNull(message = "Doctor ID is required!")
    @Positive(message = "Doctor ID must be a positive number.")
    private Long doctorId;

    @NotNull(message = "Start time is required!")
    @Future(message = "Start time must be in the future!")
    private LocalDateTime startTime;

    @Size(max = 500, message = "Reason should be under 500 characters.")
    private String reason;
}
