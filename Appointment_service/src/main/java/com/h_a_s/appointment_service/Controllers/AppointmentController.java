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
import org.springframework.security.access.prepost.PreAuthorize;
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

    // create an appointment (Admin, or Patient booking for themselves)
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PATIENT') and #appointmentRequestDto.patientId.toString() == authentication.name)")
    public ResponseEntity<AppointmentResponseDto> createAppointment(
            @Valid @RequestBody AppointmentCreationDto appointmentRequestDto,
            Principal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(appointmentRequestDto, principal));
    }

    // get all appointments (Admin only)
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PageResponseDto<AppointmentResponseDto>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(appointmentService.getAllAppointments(page, size));
    }

    // get an appointment details
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<AppointmentResponseDto> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    // get appointments of a patient
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR') or (hasAuthority('PATIENT') and #patientId.toString() == authentication.name)")
    public ResponseEntity<PageResponseDto<AppointmentResponseDto>> getAppointmentsByPatient(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId, page, size));
    }

    // get appointments of a doctor
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('DOCTOR') and #doctorId.toString() == authentication.name)")
    public ResponseEntity<PageResponseDto<AppointmentResponseDto>> getAppointmentsByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId, page, size));
    }

    // In AppointmentController.java

    // For currently logged-in Patient to view their own appointments
    @GetMapping("/patient/me")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<PageResponseDto<AppointmentResponseDto>> getMyPatientAppointments(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long patientId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId, page, size));
    }

    // For currently logged-in Doctor to view their own schedule
    @GetMapping("/doctor/me")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<PageResponseDto<AppointmentResponseDto>> getMyDoctorAppointments(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long doctorId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId, page, size));
    }

    // get booked start times of a doctor in a range
    @GetMapping("/doctor/{doctorId}/booked")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<List<LocalDateTime>> getBookedStartTimes(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(appointmentService.getBookedStartTimes(doctorId, from, to));
    }

    // cancel an appointment (Admin, Doctor, or Patient)
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<AppointmentResponseDto> cancelAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id));
    }

    // complete an appointment (Admin or Doctor)
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR')")
    public ResponseEntity<AppointmentResponseDto> completeAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.completeAppointment(id));
    }

    // mark an appointment as no-show (Admin or Doctor)
    @PatchMapping("/{id}/no-show")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR')")
    public ResponseEntity<AppointmentResponseDto> markNoShow(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.markNoShow(id));
    }

    // cancel all scheduled appointments of a doctor (Admin or the Doctor themselves)
    @PatchMapping("/doctor/{doctorId}/cancel-scheduled")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('DOCTOR') and #doctorId.toString() == authentication.name)")
    public ResponseEntity<Void> cancelScheduledAppointmentsByDoctor(@PathVariable Long doctorId) {
        appointmentService.cancelScheduledAppointmentsByDoctor(doctorId);
        return ResponseEntity.ok().build();
    }

    // cancel all scheduled appointments of a patient (Admin or the Patient themselves)
    @PatchMapping("/patient/{patientId}/cancel-scheduled")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PATIENT') and #patientId.toString() == authentication.name)")
    public ResponseEntity<Void> cancelScheduledAppointmentsByPatient(@PathVariable Long patientId) {
        appointmentService.cancelScheduledAppointmentsByPatient(patientId);
        return ResponseEntity.ok().build();
    }
}
