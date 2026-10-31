package com.h_a_s.patient_service.Dto;

import com.h_a_s.patient_service.Dto.Client.AppointmentClientDto;
import com.h_a_s.patient_service.Dto.Client.ConsultationNoteClientDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatientHistoryResponseDto {
    private PatientResponseDto patient;
    private PageResponseDto<AppointmentClientDto> appointments;
    private PageResponseDto<ConsultationNoteClientDto> consultationNotes;
}
