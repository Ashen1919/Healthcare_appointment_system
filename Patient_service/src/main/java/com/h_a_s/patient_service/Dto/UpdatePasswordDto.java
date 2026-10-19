package com.h_a_s.patient_service.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordDto {
    @NotBlank(message = "Old Password is required")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 50 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$",
            message = "Password must contain uppercase, digit, and special character(@, #, $, %, ^)"
    )
    private String oldPassword;

    @NotBlank(message = "New Password is required")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 50 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$",
            message = "Password must contain uppercase, digit, and special character(@, #, $, %, ^)"
    )
    private String newPassword;
}
