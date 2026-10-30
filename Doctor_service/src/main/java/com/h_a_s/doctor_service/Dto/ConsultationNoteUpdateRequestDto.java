package com.h_a_s.doctor_service.Dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsultationNoteUpdateRequestDto {
    private String reason;

    private String diagnosis;

    @Valid
    private List<PrescriptionDto> prescriptions;

    private String notes;
}
