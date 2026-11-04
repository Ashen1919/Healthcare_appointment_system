package com.h_a_s.patient_service.Dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientRequestDto {
    @NotBlank(message = "Email is required!")
    @Email(message = "Please enter a valid email.")
    private String email;

    @NotBlank(message = "Name is required!")
    @Size(min = 3, max = 15, message = "Name must be between 3 and 15 characters")
    @Pattern(
            regexp = "^[A-Z].{2,15}$",
            message = "First character should be uppercase & only words are allowed."
    )
    private String name;

    @NotBlank(message = "Date of birth is required!")
    @Past(message = "Date of birth must be past!")
    private LocalDate dob;

    @NotBlank(message = "Contact is required!")
    @Pattern(
            regexp = "^0[0-9]{9}$",
            message = "Please enter a valid phone number!"
    )
    private String contact;

    @NotBlank(message = "Address is required!")
    @Size(min = 5, max = 150, message = "Address should be under 150 characters.")
    private String address;
}
