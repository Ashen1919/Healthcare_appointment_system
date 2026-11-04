package com.h_a_s.doctor_service.Controllers;

import com.h_a_s.doctor_service.Dto.*;
import com.h_a_s.doctor_service.Services.DoctorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor Controllers", description = "Controllers for doctor service")
public class DoctorController {
    private final DoctorService doctorService;

    // create a doctor (Admin only)
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DoctorResponseDto> createDoctor(
            @Valid
            @RequestBody DoctorCreateRequestDto doctorCreateRequestDto
            ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.createDoctor(doctorCreateRequestDto));
    }

    // get all doctors' details (Any authenticated user)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<PageResponseDto<DoctorResponseDto>> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(doctorService.getAllDoctors(page, size));
    }

    // get doctor by ID (Any authenticated user)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<DoctorResponseDto> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    // get current logged in doctor's details
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<DoctorResponseDto> getCurrentDoctor(Principal principal) {
        return ResponseEntity.ok(doctorService.currentDoctor(principal));
    }

    // update doctor details
    @PatchMapping
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<DoctorResponseDto> updateDoctor(
            @Valid
            @RequestBody DoctorUpdateRequestDto doctorUpdateRequestDto,
            Principal principal
            ) {
        return ResponseEntity.ok(doctorService.updateDoctor(doctorUpdateRequestDto, principal));
    }

    // add time-off for a doctor (Admin or the Doctor themselves)
    @PostMapping("/{id}/time-off")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('DOCTOR') and #id.toString() == authentication.name)")
    public ResponseEntity<DoctorResponseDto> addTimeOff(
            @PathVariable Long id,
            @Valid @RequestBody TimeOffRequestDto timeOffRequestDto,
            Principal principal
    ) {
        return ResponseEntity.ok(doctorService.addTimeOff(id, timeOffRequestDto, principal));
    }

    // deactivate a doctor and cancel scheduled appointments
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('DOCTOR') and #id.toString() == authentication.name)")
    public ResponseEntity<DoctorResponseDto> deactivateDoctor(
            @PathVariable Long id,
            Principal principal
    ) {
        return ResponseEntity.ok(doctorService.deactivateDoctor(id, principal));
    }

    // activate a doctor (Admin only)
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DoctorResponseDto> activateDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.activateDoctor(id));
    }
}
