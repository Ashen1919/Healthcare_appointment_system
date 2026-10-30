package com.h_a_s.appointment_service.Dto.Client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorClientDto {
    private Long id;
    private String email;
    private String name;
    private String specialty;
    private String contact;
    private boolean active;
    private List<ScheduleSlotDto> schedule;
    private List<TimeOffDto> timeOff;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleSlotDto {
        private DayOfWeek dayOfWeek;
        private LocalTime start;
        private LocalTime end;
        private int slotMinutes;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeOffDto {
        private Instant from;
        private Instant to;
        private String reason;
    }
}
