package com.auth_service.auth_service.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter valid email")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
