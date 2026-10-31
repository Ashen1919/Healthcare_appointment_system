package com.h_a_s.patient_service.Dto.Client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsultationNoteClientDto {
    private Long id;
    private Long appointmentId;
    private Long doctorId;
    private Long patientId;
    private String reason;
    private String diagnosis;
    private List<PrescriptionDto> prescriptions;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PrescriptionDto {
        private String drug;
        private String dosage;
        private int numberOfDays;
    }
}
