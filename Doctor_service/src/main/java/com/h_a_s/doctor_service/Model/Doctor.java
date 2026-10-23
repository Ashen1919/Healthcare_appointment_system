package com.h_a_s.doctor_service.Model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "doctors")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Doctor {
    @Id
    private Long id;

    private String email;

    private String password;

    private String name;

    @Indexed
    private String specialty;

    private String contact;

    @NotNull
    private boolean active;

    @Builder.Default
    private List<ScheduleSlot> schedule = new ArrayList<>();

    @Builder.Default
    private List<TimeOff> timeOff = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class ScheduleSlot {
        private DayOfWeek dayOfWeek;
        private LocalTime start;
        private LocalTime end;
        private int slotMinutes;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class TimeOff {
        private Instant from;
        private Instant to;
        private String reason;
    }
}
