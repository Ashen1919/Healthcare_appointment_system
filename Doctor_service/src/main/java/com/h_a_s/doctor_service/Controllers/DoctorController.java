package com.h_a_s.doctor_service.Controllers;

import com.h_a_s.doctor_service.Dto.*;
import com.h_a_s.doctor_service.Services.DoctorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor Controllers", description = "Controllers for doctor service")
public class DoctorController {
    private final DoctorService doctorService;

    // create a doctor
    @PostMapping
    public ResponseEntity<DoctorResponseDto> createDoctor(
            @Valid
            @RequestBody DoctorCreateRequestDto doctorCreateRequestDto
            ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.createDoctor(doctorCreateRequestDto));
    }

    // get all doctors' details
    @GetMapping
    public ResponseEntity<PageResponseDto<DoctorResponseDto>> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(doctorService.getAllDoctors(page, size));
    }

    // get doctor by ID
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDto> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    // get current logged in doctor's details
    @GetMapping("/me")
    public ResponseEntity<DoctorResponseDto> getCurrentDoctor(Principal principal) {
        return ResponseEntity.ok(doctorService.currentDoctor(principal));
    }

    // update doctor details
    @PatchMapping
    public ResponseEntity<DoctorResponseDto> updateDoctor(
            @Valid
            @RequestBody DoctorUpdateRequestDto doctorUpdateRequestDto,
            Principal principal
            ) {
        return ResponseEntity.ok(doctorService.updateDoctor(doctorUpdateRequestDto, principal));
    }

    // update password
    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @Valid
            @RequestBody UpdatePasswordDto updatePasswordDto,
            Principal principal
            ) {
        return ResponseEntity.ok().build();
    }
}
