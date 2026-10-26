package com.h_a_s.appointment_service.Controllers;

import com.h_a_s.appointment_service.Dto.AppointmentCreationDto;
import com.h_a_s.appointment_service.Dto.AppointmentResponseDto;
import com.h_a_s.appointment_service.Dto.PageResponseDto;
import com.h_a_s.appointment_service.Services.AppointmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment Controllers", description = "Controllers for appointment service")
public class AppointmentController {
    private final AppointmentService appointmentService;

    // create an appointment
    @PostMapping
    public ResponseEntity<AppointmentResponseDto> createAppointment(
            @Valid @RequestBody AppointmentCreationDto appointmentRequestDto,
            Principal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(appointmentRequestDto, principal));
    }

    // get all appointments
    @GetMapping
    public ResponseEntity<PageResponseDto<AppointmentResponseDto>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(appointmentService.getAllAppointments(page, size));
    }

    // get an appointment details
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    // get appointments of a patient
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<PageResponseDto<AppointmentResponseDto>> getAppointmentsByPatient(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId, page, size));
    }

    // get appointments of a doctor
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<PageResponseDto<AppointmentResponseDto>> getAppointmentsByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId, page, size));
    }

    // get booked start times of a doctor in a range
    @GetMapping("/doctor/{doctorId}/booked")
    public ResponseEntity<List<LocalDateTime>> getBookedStartTimes(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(appointmentService.getBookedStartTimes(doctorId, from, to));
    }

    // cancel an appointment
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancelAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id));
    }

    // complete an appointment
    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponseDto> completeAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.completeAppointment(id));
    }

    // mark an appointment as no-show
    @PatchMapping("/{id}/no-show")
    public ResponseEntity<AppointmentResponseDto> markNoShow(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.markNoShow(id));
    }
}
