package com.h_a_s.patient_service.services;

import com.h_a_s.patient_service.Dto.PageResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    // create patient
    public PatientResponseDto createPatient(PatientRequestDto patientRequestDto) {
        Patient patient = modelMapper.map(patientRequestDto, Patient.class);
        patient.setPassword(passwordEncoder.encode(patientRequestDto.getPassword()));
        patient.setRole(Roles.PATIENT);
        Patient savePatient = patientRepo.save(patient);
        return modelMapper.map(savePatient, PatientResponseDto.class);
    }

    // get all patients
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
    public PatientResponseDto getPatientById(Long id) {
        Patient existingPatient = patientRepo.findById(id).orElseThrow(() ->
            new ResourceNotFoundException("Patient can't find with id: " + id));
        return modelMapper.map(existingPatient, PatientResponseDto.class);
    }

    // get logged in patient's details
    public PatientResponseDto getCurrentPatientDetails(Principal principal) {
        Long patientId = Long.valueOf(principal.getName());
        Patient currentPatient = patientRepo.findById(patientId)
                .orElseThrow(() -> new UsernameNotFoundException("Patient Not found with id " + patientId));

        return modelMapper.map(currentPatient, PatientResponseDto.class);
    }

    // update a patient
    public PatientResponseDto updatePatient(UpdatePatientRequestDto updatePatientRequestDto, Principal principal) {
        Long patientID = Long.valueOf(principal.getName());
        Patient existingPatient = patientRepo.findById(patientID)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found with the id " + patientID));

        existingPatient.setAddress(updatePatientRequestDto.getAddress());
        existingPatient.setContact(updatePatientRequestDto.getContact());
        existingPatient.setDob(updatePatientRequestDto.getDob());
        existingPatient.setName(updatePatientRequestDto.getName());
        existingPatient.setUpdated_at(LocalDateTime.now());

        Patient updatePatient = patientRepo.save(existingPatient);
        return modelMapper.map(updatePatient, PatientResponseDto.class);
    }

    // update patient's password
    public void updatePassword(String oldPassword, String newPassword, Principal principal) {
        Long patientID = Long.valueOf(principal.getName());
        Patient existingPatient = patientRepo.findById(patientID)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found with the id " + patientID));

        if(!passwordEncoder.matches(oldPassword, existingPatient.getPassword())){
            throw new RuntimeException("Old Password is incorrect");
        }

        existingPatient.setPassword(passwordEncoder.encode(newPassword));
        existingPatient.setUpdated_at(LocalDateTime.now());
        patientRepo.save(existingPatient);
    }

}
