package com.h_a_s.patient_service.controllers;

import com.h_a_s.patient_service.Dto.*;
import com.h_a_s.patient_service.services.PatientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("api/v1/patients")
@Tag(name = "Patient Controllers", description = "Controllers for patient service")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;

    // create a patient (Admin only)
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PatientResponseDto> createPatient(@Valid @RequestBody PatientRequestDto patientRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.createPatient(patientRequestDto));
    }

    // get all patients (Admin or Doctor)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR')")
    public ResponseEntity<PageResponseDto<PatientResponseDto>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(patientService.getAllPatients(page, size));
    }

    // get a patient details (Admin, Doctor, or the Patient themselves)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR') or (hasAuthority('PATIENT') and #id.toString() == authentication.name)")
    public ResponseEntity<PatientResponseDto> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    // get current logged in patient
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<PatientResponseDto> getCurrentPatient(Principal principal) {
        return ResponseEntity.ok(patientService.getCurrentPatientDetails(principal));
    }

    // update a patient
    @PatchMapping
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<PatientResponseDto> updatePatient(
            @Valid
            @RequestBody UpdatePatientRequestDto updatePatientRequestDto,
            Principal principal
            ) {
        return ResponseEntity.ok(patientService.updatePatient(updatePatientRequestDto, principal));
    }

    // deactivate a patient and cancel their scheduled appointments (Admin or the Patient themselves)
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PATIENT') and #id.toString() == authentication.name)")
    public ResponseEntity<PatientResponseDto> deactivatePatient(
            @PathVariable Long id,
            Principal principal
    ) {
        return ResponseEntity.ok(patientService.deactivatePatient(id, principal));
    }

    // activate a patient (Admin only)
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PatientResponseDto> activatePatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.activatePatient(id));
    }

    // get aggregated history of a patient (Admin, Doctor, or the Patient themselves)
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR') or (hasAuthority('PATIENT') and #id.toString() == authentication.name)")
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
    @PreAuthorize("hasAuthority('PATIENT')")
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
