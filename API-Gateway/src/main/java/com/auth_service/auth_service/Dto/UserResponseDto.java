package com.auth_service.auth_service.Dto;

import com.auth_service.auth_service.Model.Roles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private long id;
    private String email;
    private Roles role;
    private boolean enabled;
    private boolean is_password_changed;
    private LocalDateTime last_login_at;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
