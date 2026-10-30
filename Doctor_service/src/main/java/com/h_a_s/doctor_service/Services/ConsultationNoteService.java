package com.h_a_s.doctor_service.Services;

import com.h_a_s.doctor_service.Client.AppointmentClient;
import com.h_a_s.doctor_service.Dto.Client.AppointmentClientDto;
import com.h_a_s.doctor_service.Dto.Client.Status;
import com.h_a_s.doctor_service.Dto.*;
import com.h_a_s.doctor_service.Exceptions.ResourceNotFoundException;
import com.h_a_s.doctor_service.Model.ConsultationNote;
import com.h_a_s.doctor_service.Repository.ConsultationNoteRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsultationNoteService {
    private final ConsultationNoteRepo consultationNoteRepo;
    private final AppointmentClient appointmentClient;
    private final ConsultationNoteIdGenerator consultationNoteIdGenerator;
    private final ModelMapper modelMapper;

    // Create consultation note
    public ConsultationNoteResponseDto createNote(ConsultationNoteCreateRequestDto requestDto, Principal principal) {
        // If caller is authenticated, verify doctor identity
        if (principal != null && principal.getName() != null) {
            Long currentDoctorId = Long.valueOf(principal.getName());
            if (!currentDoctorId.equals(requestDto.getDoctorId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not authorized to create consultation notes for another doctor.");
            }
        }

        // Check if consultation note already exists for this appointment
        if (consultationNoteRepo.existsByAppointmentId(requestDto.getAppointmentId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A consultation note has already been recorded for appointment id: " + requestDto.getAppointmentId());
        }

        // Fetch appointment details from Appointment Service via OpenFeign
        AppointmentClientDto appointment = appointmentClient.getAppointmentById(requestDto.getAppointmentId());
        if (appointment == null) {
            throw new ResourceNotFoundException("Appointment not found with id: " + requestDto.getAppointmentId());
        }

        // Check that doctorId matches
        if (!requestDto.getDoctorId().equals(appointment.getDoctorId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Doctor ID does not match the appointment's assigned doctor.");
        }

        // Check that patientId matches
        if (!requestDto.getPatientId().equals(appointment.getPatientId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Patient ID does not match the appointment's patient.");
        }

        // Check appointment status (must be COMPLETED)
        if (appointment.getStatus() != Status.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Consultation notes can only be created for COMPLETED appointments. Current appointment status: "
                            + appointment.getStatus());
        }

        // Map and persist
        List<ConsultationNote.Prescription> prescriptions = requestDto.getPrescriptions() == null
                ? new ArrayList<>()
                : requestDto.getPrescriptions().stream()
                .map(p -> ConsultationNote.Prescription.builder()
                        .drug(p.getDrug())
                        .dosage(p.getDosage())
                        .numberOfDays(p.getNumberOfDays())
                        .build())
                .toList();

        ConsultationNote note = ConsultationNote.builder()
                .id(consultationNoteIdGenerator.nextId())
                .appointmentId(requestDto.getAppointmentId())
                .doctorId(requestDto.getDoctorId())
                .patientId(requestDto.getPatientId())
                .reason(requestDto.getReason())
                .diagnosis(requestDto.getDiagnosis())
                .prescriptions(prescriptions)
                .notes(requestDto.getNotes())
                .build();

        ConsultationNote savedNote = consultationNoteRepo.insert(note);
        return modelMapper.map(savedNote, ConsultationNoteResponseDto.class);
    }

    // Get consultation note by ID
    public ConsultationNoteResponseDto getNoteById(Long id) {
        ConsultationNote note = consultationNoteRepo.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Consultation note not found with id: " + id));

        return modelMapper.map(note, ConsultationNoteResponseDto.class);
    }

    // Get consultation note by appointment ID
    public ConsultationNoteResponseDto getNoteByAppointmentId(Long appointmentId) {
        ConsultationNote note = consultationNoteRepo.findByAppointmentId(appointmentId).orElseThrow(() ->
                new ResourceNotFoundException("Consultation note not found for appointment id: " + appointmentId));

        return modelMapper.map(note, ConsultationNoteResponseDto.class);
    }

    // Get all consultation notes of a patient
    public PageResponseDto<ConsultationNoteResponseDto> getNotesByPatient(Long patientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ConsultationNote> notes = consultationNoteRepo.findByPatientId(patientId, pageable);

        return toPageResponse(notes);
    }

    // Get all consultation notes created by a doctor
    public PageResponseDto<ConsultationNoteResponseDto> getNotesByDoctor(Long doctorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ConsultationNote> notes = consultationNoteRepo.findByDoctorId(doctorId, pageable);

        return toPageResponse(notes);
    }

    // Update consultation note
    public ConsultationNoteResponseDto updateNote(Long id, ConsultationNoteUpdateRequestDto requestDto, Principal principal) {
        ConsultationNote note = consultationNoteRepo.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Consultation note not found with id: " + id));

        if (principal != null && principal.getName() != null) {
            Long currentDoctorId = Long.valueOf(principal.getName());
            if (!currentDoctorId.equals(note.getDoctorId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not authorized to update another doctor's consultation note.");
            }
        }

        if (requestDto.getReason() != null) {
            note.setReason(requestDto.getReason());
        }
        if (requestDto.getDiagnosis() != null) {
            note.setDiagnosis(requestDto.getDiagnosis());
        }
        if (requestDto.getNotes() != null) {
            note.setNotes(requestDto.getNotes());
        }
        if (requestDto.getPrescriptions() != null) {
            note.setPrescriptions(requestDto.getPrescriptions().stream()
                    .map(p -> ConsultationNote.Prescription.builder()
                            .drug(p.getDrug())
                            .dosage(p.getDosage())
                            .numberOfDays(p.getNumberOfDays())
                            .build())
                    .toList());
        }

        ConsultationNote savedNote = consultationNoteRepo.save(note);
        return modelMapper.map(savedNote, ConsultationNoteResponseDto.class);
    }

    private PageResponseDto<ConsultationNoteResponseDto> toPageResponse(Page<ConsultationNote> notes) {
        List<ConsultationNoteResponseDto> content = notes.getContent()
                .stream()
                .map(note -> modelMapper.map(note, ConsultationNoteResponseDto.class))
                .toList();

        return new PageResponseDto<>(
                content,
                notes.getNumber(),
                notes.getSize(),
                notes.getTotalElements(),
                notes.getTotalPages(),
                notes.isLast()
        );
    }
}
