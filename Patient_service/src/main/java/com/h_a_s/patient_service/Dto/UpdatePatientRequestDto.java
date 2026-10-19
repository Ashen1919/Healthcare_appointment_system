package com.h_a_s.patient_service.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePatientRequestDto {
    @Size(min = 3, max = 15, message = "Name must be between 3 and 15 characters")
    @Pattern(
            regexp = "^[A-Z].{2,15}$",
            message = "First character should be uppercase & only words are allowed."
    )
    private String name;

    @Past(message = "Date of birth must be past!")
    private LocalDate dob;

    @Pattern(
            regexp = "^(?=0[0-9]{9})$",
            message = "Please enter a valid phone number!"
    )
    private String contact;

    @Size(min = 5, max = 150, message = "Address should be under 150 characters.")
    private String address;
}
