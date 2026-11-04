package com.auth_service.auth_service.Dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class PatientUserCreateRequestDto extends UserCreateRequestDto {

    @NotBlank(message = "Name is required!")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z].{2,49}$", message = "First character should be uppercase")
    private String name;

    @NotNull(message = "Date of birth is required!")
    @Past(message = "Date of birth must be past!")
    private LocalDate dob;

    @NotBlank(message = "Contact is required!")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Please enter a valid phone number!")
    private String contact;

    @NotBlank(message = "Address is required!")
    @Size(min = 5, max = 150, message = "Address should be under 150 characters.")
    private String address;
}