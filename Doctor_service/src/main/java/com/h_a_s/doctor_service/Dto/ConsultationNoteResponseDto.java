package com.h_a_s.doctor_service.Dto;

import com.h_a_s.doctor_service.Model.ConsultationNote;
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
public class ConsultationNoteResponseDto {
    private Long id;
    private Long appointmentId;
    private Long doctorId;
    private Long patientId;
    private String reason;
    private String diagnosis;
    private List<ConsultationNote.Prescription> prescriptions;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
