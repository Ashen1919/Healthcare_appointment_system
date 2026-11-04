package com.auth_service.auth_service.Controller;

import com.auth_service.auth_service.Dto.*;
import com.auth_service.auth_service.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponseDto> createAdmin(@Valid @RequestBody UserCreateRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createAdmin(dto));
    }

    @PostMapping("/patient")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponseDto> createPatient(@Valid @RequestBody PatientUserCreateRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createPatient(dto));
    }

    @PostMapping("/doctor")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponseDto> createDoctor(@Valid @RequestBody DoctorUserCreateRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createDoctor(dto));
    }
}