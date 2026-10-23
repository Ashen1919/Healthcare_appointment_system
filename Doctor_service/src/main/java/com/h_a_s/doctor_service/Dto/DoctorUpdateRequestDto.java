package com.h_a_s.doctor_service.Dto;

import com.h_a_s.doctor_service.Model.Doctor;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorUpdateRequestDto {
    @NotBlank(message = "Email is required!")
    @Email(message = "Please enter a valid email address!")
    private String email;

    @NotBlank(message = "Name is required!")
    @Size(min = 3, max = 50, message = "Name should contain minimum 3 and maximum 50 characters.")
    @Pattern(
            regexp = "^[A-Z].{2,50}$",
            message = "First character should be uppercase & only words are allowed."
    )
    private String name;

    @NotBlank(message = "Speciality is required!")
    @Size(min = 3, max = 50, message = "Speciality should contain minimum 3 and maximum 50 characters.")
    @Pattern(
            regexp = "^[A-Z].{2,50}$",
            message = "First character should be uppercase & only words are allowed."
    )
    private String specialty;

    @NotBlank(message = "Contact is required!")
    @Pattern(
            regexp = "^0[0-9]{9}$",
            message = "Please enter a valid phone number!"
    )
    private String contact;

    @NotEmpty(message = "Schedule should be not empty!")
    private List<Doctor.ScheduleSlot> schedule;

    private List<Doctor.TimeOff> timeOff;
}
