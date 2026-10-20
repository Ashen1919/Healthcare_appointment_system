package com.h_a_s.patient_service.controllers;

import com.h_a_s.patient_service.Dto.*;
import com.h_a_s.patient_service.services.PatientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("api/v1/patients")
@Tag(name = "Patient Controllers", description = "Controllers for patient service")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;

    // create a patient
    @PostMapping
    public ResponseEntity<PatientResponseDto> createPatient(@Valid @RequestBody PatientRequestDto patientRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.createPatient(patientRequestDto));
    }

    // get all patients
    @GetMapping
    public ResponseEntity<PageResponseDto<PatientResponseDto>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(patientService.getAllPatients(page, size));
    }

    // get a patient details
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDto> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    // get current logged in patient
    @GetMapping("/me")
    public ResponseEntity<PatientResponseDto> getCurrentPatient(Principal principal) {
        return ResponseEntity.ok(patientService.getCurrentPatientDetails(principal));
    }

    // update a patient
    @PatchMapping
    public ResponseEntity<PatientResponseDto> updatePatient(
            @Valid
            @RequestBody UpdatePatientRequestDto updatePatientRequestDto,
            Principal principal
            ) {
        return ResponseEntity.ok(patientService.updatePatient(updatePatientRequestDto, principal));
    }

    // update patient's password
    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @Valid
            @RequestBody UpdatePasswordDto updatePasswordDto,
            Principal principal
            ) {
        patientService.updatePassword(updatePasswordDto.getOldPassword(), updatePasswordDto.getNewPassword(), principal);
        return ResponseEntity.ok().build();
    }
}
