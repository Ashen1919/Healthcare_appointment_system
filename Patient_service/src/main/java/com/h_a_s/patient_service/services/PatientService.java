package com.h_a_s.patient_service.services;

import com.h_a_s.patient_service.Client.AppointmentClient;
import com.h_a_s.patient_service.Client.DoctorClient;
import com.h_a_s.patient_service.Dto.Client.AppointmentClientDto;
import com.h_a_s.patient_service.Dto.Client.ConsultationNoteClientDto;
import com.h_a_s.patient_service.Dto.PageResponseDto;
import com.h_a_s.patient_service.Dto.PatientHistoryResponseDto;
import com.h_a_s.patient_service.Dto.PatientRequestDto;
import com.h_a_s.patient_service.Dto.PatientResponseDto;
import com.h_a_s.patient_service.Dto.UpdatePatientRequestDto;
import com.h_a_s.patient_service.Exceptions.ResourceNotFoundException;
import com.h_a_s.patient_service.Repository.PatientRepo;
import com.h_a_s.patient_service.model.Patient;
import com.h_a_s.patient_service.model.Roles;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepo patientRepo;
    private final ModelMapper modelMapper;
    private final AppointmentClient appointmentClient;
    private final DoctorClient doctorClient;

    // create patient
    @CacheEvict(value = "patients", allEntries = true)
    public PatientResponseDto createPatient(PatientRequestDto patientRequestDto) {
        Patient patient = modelMapper.map(patientRequestDto, Patient.class);
        patient.setRole(Roles.PATIENT);
        patient.setActive(true);
        Patient savePatient = patientRepo.save(patient);
        return modelMapper.map(savePatient, PatientResponseDto.class);
    }

    // get all patients
    @Cacheable(value = "patients", key = "'all'")
    public PageResponseDto<PatientResponseDto> getAllPatients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Patient> patients = patientRepo.findAll(pageable);

        List<PatientResponseDto> content = patients.getContent()
                .stream()
                .map(patient -> modelMapper.map(patient, PatientResponseDto.class))
                .toList();

        return new PageResponseDto<>(
                content,
                patients.getNumber(),
                patients.getSize(),
                patients.getTotalElements(),
                patients.getTotalPages(),
                patients.isLast()
        );
    }

    // get specific patient details
    @Cacheable(value = "patients", key = "'all'")
    public PatientResponseDto getPatientById(Long id) {
        Patient existingPatient = patientRepo.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Patient can't find with id: " + id));
        return modelMapper.map(existingPatient, PatientResponseDto.class);
    }

    // get logged in patient's details
    @Cacheable(value = "patients", key = "'all'")
    public PatientResponseDto getCurrentPatientDetails(Principal principal) {
        Long patientId = Long.valueOf(principal.getName());
        Patient currentPatient = patientRepo.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient Not found with id " + patientId));

        return modelMapper.map(currentPatient, PatientResponseDto.class);
    }

    // update a patient
    @CacheEvict(value = "patients", allEntries = true)
    public PatientResponseDto updatePatient(UpdatePatientRequestDto updatePatientRequestDto, Principal principal) {
        Long patientID = Long.valueOf(principal.getName());
        Patient existingPatient = patientRepo.findById(patientID)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with the id " + patientID));

        existingPatient.setAddress(updatePatientRequestDto.getAddress());
        existingPatient.setContact(updatePatientRequestDto.getContact());
        existingPatient.setDob(updatePatientRequestDto.getDob());
        existingPatient.setName(updatePatientRequestDto.getName());
        existingPatient.setUpdated_at(LocalDateTime.now());

        Patient updatePatient = patientRepo.save(existingPatient);
        return modelMapper.map(updatePatient, PatientResponseDto.class);
    }

    // Deactivate patient and cancel all their SCHEDULED appointments in Appointment Service
    @CacheEvict(value = "patients", allEntries = true)
    public PatientResponseDto deactivatePatient(Long id, Principal principal) {
        if (principal != null && principal.getName() != null) {
            Long currentPatientId = Long.valueOf(principal.getName());
            if (!currentPatientId.equals(id)) {
                // If needed, check admin role
            }
        }

        Patient patient = patientRepo.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Patient can't find with id: " + id));

        if (patient.isActive()) {
            patient.setActive(false);
            patient.setUpdated_at(LocalDateTime.now());

            // Cancel all scheduled appointments in Appointment Service via OpenFeign
            appointmentClient.cancelScheduledAppointmentsByPatient(id);

            patient = patientRepo.save(patient);
        }

        return modelMapper.map(patient, PatientResponseDto.class);
    }

    // Activate a patient
    @CacheEvict(value = "patients", allEntries = true)
    public PatientResponseDto activatePatient(Long id) {
        Patient patient = patientRepo.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Patient can't find with id: " + id));

        patient.setActive(true);
        patient.setUpdated_at(LocalDateTime.now());
        Patient saved = patientRepo.save(patient);
        return modelMapper.map(saved, PatientResponseDto.class);
    }

    // Get aggregated patient history (Appointments & Consultation Notes)
    @Cacheable(value = "patients", key = "'all'")
    public PatientHistoryResponseDto getPatientHistory(Long id, int appointmentPage, int appointmentSize,
                                                       int notePage, int noteSize, Principal principal) {
        if (principal != null && principal.getName() != null) {
            Long currentPatientId = Long.valueOf(principal.getName());
            if (!currentPatientId.equals(id)) {
                // Permit admin, or restrict if patient is accessing another patient's records
            }
        }

        Patient patient = patientRepo.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Patient can't find with id: " + id));

        PatientResponseDto patientDto = modelMapper.map(patient, PatientResponseDto.class);

        // Fetch appointments via OpenFeign
        PageResponseDto<AppointmentClientDto> appointments =
                appointmentClient.getAppointmentsByPatient(id, appointmentPage, appointmentSize);

        // Fetch consultation notes via OpenFeign
        PageResponseDto<ConsultationNoteClientDto> consultationNotes =
                doctorClient.getConsultationNotesByPatient(id, notePage, noteSize);

        return PatientHistoryResponseDto.builder()
                .patient(patientDto)
                .appointments(appointments)
                .consultationNotes(consultationNotes)
                .build();
    }

    // Get current logged-in patient's history
    @Cacheable(value = "patients", key = "'all'")
    public PatientHistoryResponseDto getCurrentPatientHistory(Principal principal,
                                                              int appointmentPage, int appointmentSize,
                                                              int notePage, int noteSize) {
        Long patientId = Long.valueOf(principal.getName());
        return getPatientHistory(patientId, appointmentPage, appointmentSize, notePage, noteSize, principal);
    }
}
