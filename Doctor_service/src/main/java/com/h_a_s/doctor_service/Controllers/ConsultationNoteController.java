package com.h_a_s.doctor_service.Controllers;

import com.h_a_s.doctor_service.Dto.ConsultationNoteCreateRequestDto;
import com.h_a_s.doctor_service.Dto.ConsultationNoteResponseDto;
import com.h_a_s.doctor_service.Dto.ConsultationNoteUpdateRequestDto;
import com.h_a_s.doctor_service.Dto.PageResponseDto;
import com.h_a_s.doctor_service.Services.ConsultationNoteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/consultation-notes")
@RequiredArgsConstructor
@Tag(name = "Consultation Note Controllers", description = "Controllers for consultation note operations")
public class ConsultationNoteController {
    private final ConsultationNoteService consultationNoteService;

    // Create a consultation note
    @PostMapping
    public ResponseEntity<ConsultationNoteResponseDto> createConsultationNote(
            @Valid @RequestBody ConsultationNoteCreateRequestDto requestDto,
            Principal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consultationNoteService.createNote(requestDto, principal));
    }

    // Get consultation note by ID
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationNoteResponseDto> getNoteById(@PathVariable Long id) {
        return ResponseEntity.ok(consultationNoteService.getNoteById(id));
    }

    // Get consultation note by appointment ID
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ConsultationNoteResponseDto> getNoteByAppointmentId(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(consultationNoteService.getNoteByAppointmentId(appointmentId));
    }

    // Get consultation notes by patient ID
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<PageResponseDto<ConsultationNoteResponseDto>> getNotesByPatient(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(consultationNoteService.getNotesByPatient(patientId, page, size));
    }

    // Get consultation notes by doctor ID
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<PageResponseDto<ConsultationNoteResponseDto>> getNotesByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(consultationNoteService.getNotesByDoctor(doctorId, page, size));
    }

    // Update consultation note
    @PatchMapping("/{id}")
    public ResponseEntity<ConsultationNoteResponseDto> updateConsultationNote(
            @PathVariable Long id,
            @Valid @RequestBody ConsultationNoteUpdateRequestDto requestDto,
            Principal principal
    ) {
        return ResponseEntity.ok(consultationNoteService.updateNote(id, requestDto, principal));
    }
}
