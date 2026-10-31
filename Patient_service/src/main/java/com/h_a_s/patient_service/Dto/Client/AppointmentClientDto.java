package com.h_a_s.patient_service.Dto.Client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentClientDto {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Status status;
    private String reason;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
