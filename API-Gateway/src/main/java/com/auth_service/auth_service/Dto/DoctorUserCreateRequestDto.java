package com.auth_service.auth_service.Dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class DoctorUserCreateRequestDto extends UserCreateRequestDto {

    @NotBlank(message = "Name is required!")
    @Size(min = 3, max = 50, message = "Name should contain minimum 3 and maximum 50 characters.")
    @Pattern(regexp = "^[A-Z].{2,49}$", message = "First character should be uppercase")
    private String name;

    @NotBlank(message = "Specialty is required!")
    @Size(min = 3, max = 50, message = "Specialty should contain minimum 3 and maximum 50 characters.")
    private String specialty;

    @NotBlank(message = "Contact is required!")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Please enter a valid phone number!")
    private String contact;

    @NotEmpty(message = "Schedule should not be empty!")
    private List<ScheduleSlotDto> schedule;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleSlotDto {
        private DayOfWeek dayOfWeek;
        private LocalTime start;
        private LocalTime end;
        private int slotMinutes;
    }
}