package com.h_a_s.doctor_service.Model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "consultation_notes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsultationNote {
    @Id
    private Long id;

    @Indexed(unique = true)
    private Long appointmentId;

    @Indexed
    private Long doctorId;

    @Indexed
    private Long patientId;

    private String reason;

    private String diagnosis;

    @Builder.Default
    private List<Prescription> prescriptions = new ArrayList<>();

    private String notes;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Prescription {
        private String drug;
        private String dosage;
        private int numberOfDays;
    }
}
