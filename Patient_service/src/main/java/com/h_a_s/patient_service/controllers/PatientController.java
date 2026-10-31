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

    // deactivate a patient and cancel their scheduled appointments
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<PatientResponseDto> deactivatePatient(
            @PathVariable Long id,
            Principal principal
    ) {
        return ResponseEntity.ok(patientService.deactivatePatient(id, principal));
    }

    // activate a patient
    @PatchMapping("/{id}/activate")
    public ResponseEntity<PatientResponseDto> activatePatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.activatePatient(id));
    }

    // get aggregated history of a patient (appointments & consultation notes)
    @GetMapping("/{id}/history")
    public ResponseEntity<PatientHistoryResponseDto> getPatientHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int appointmentPage,
            @RequestParam(defaultValue = "10") int appointmentSize,
            @RequestParam(defaultValue = "0") int notePage,
            @RequestParam(defaultValue = "10") int noteSize,
            Principal principal
    ) {
        return ResponseEntity.ok(patientService.getPatientHistory(
                id, appointmentPage, appointmentSize, notePage, noteSize, principal));
    }

    // get aggregated history of currently logged-in patient
    @GetMapping("/me/history")
    public ResponseEntity<PatientHistoryResponseDto> getCurrentPatientHistory(
            @RequestParam(defaultValue = "0") int appointmentPage,
            @RequestParam(defaultValue = "10") int appointmentSize,
            @RequestParam(defaultValue = "0") int notePage,
            @RequestParam(defaultValue = "10") int noteSize,
            Principal principal
    ) {
        return ResponseEntity.ok(patientService.getCurrentPatientHistory(
                principal, appointmentPage, appointmentSize, notePage, noteSize));
    }
}
