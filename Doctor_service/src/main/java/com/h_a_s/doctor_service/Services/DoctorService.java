package com.h_a_s.doctor_service.Services;

import com.h_a_s.doctor_service.Dto.DoctorCreateRequestDto;
import com.h_a_s.doctor_service.Dto.DoctorResponseDto;
import com.h_a_s.doctor_service.Dto.DoctorUpdateRequestDto;
import com.h_a_s.doctor_service.Dto.PageResponseDto;
import com.h_a_s.doctor_service.Exceptions.ResourceNotFoundException;
import com.h_a_s.doctor_service.Model.Doctor;
import com.h_a_s.doctor_service.Repository.DoctorRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.print.Doc;
import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorService {
    private final DoctorRepo doctorRepo;
    private final ModelMapper modelMapper;
    private final DoctorIdGenerator doctorIdGenerator;
    private final PasswordEncoder passwordEncoder;

    // Create a doctor
    public DoctorResponseDto createDoctor(DoctorCreateRequestDto requestDto) {
        if(doctorRepo.existsByEmail(requestDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor is already exist.");
        }

        Doctor doctor = modelMapper.map(requestDto, Doctor.class);
        doctor.setId(doctorIdGenerator.nextId());
        doctor.setActive(true);
        doctor.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        Doctor saveDoctor = doctorRepo.insert(doctor);
        return modelMapper.map(saveDoctor, DoctorResponseDto.class);
    }

    // get all doctors details
    public PageResponseDto<DoctorResponseDto> getAllDoctors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Doctor> doctors = doctorRepo.findAll(pageable);

        List<DoctorResponseDto> content = doctors.getContent()
                .stream()
                .map(doctor -> modelMapper.map(doctor, DoctorResponseDto.class))
                .toList();

        return new PageResponseDto<>(
                content,
                doctors.getNumber(),
                doctors.getSize(),
                doctors.getTotalElements(),
                doctors.getTotalPages(),
                doctors.isLast()
        );
    }

    // get a doctor by ID
    public DoctorResponseDto getDoctorById(Long id) {
        Doctor doctor = doctorRepo.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Doctor not found with the id " + id));

        return modelMapper.map(doctor, DoctorResponseDto.class);
    }

    // get current logged in doctor details
    public DoctorResponseDto currentDoctor(Principal principal) {
        Long doctorId = Long.valueOf(principal.getName());
        Doctor currentDoctor = doctorRepo.findById(doctorId).orElseThrow(() ->
                new ResourceNotFoundException("Doctor not found with the id " + doctorId));

        return modelMapper.map(currentDoctor, DoctorResponseDto.class);
    }

    // update a doctor
    public DoctorResponseDto updateDoctor(DoctorUpdateRequestDto doctorUpdateRequestDto, Principal principal) {
        Long doctorId = Long.valueOf(principal.getName());
        Doctor currentDoctor = doctorRepo.findById(doctorId).orElseThrow(() ->
                new ResourceNotFoundException("Doctor not found with the id " + doctorId));

        currentDoctor.setEmail(doctorUpdateRequestDto.getEmail());
        currentDoctor.setName(doctorUpdateRequestDto.getName());
        currentDoctor.setContact(doctorUpdateRequestDto.getContact());
        currentDoctor.setSpecialty(doctorUpdateRequestDto.getSpecialty());
        currentDoctor.setSchedule(doctorUpdateRequestDto.getSchedule());

        Doctor saveDoctor = doctorRepo.insert(currentDoctor);
        return modelMapper.map(saveDoctor, DoctorResponseDto.class);
    }

    // update doctor's password
    public void updateDoctorPassword(String OldPassword, String NewPassword, Principal principal) {
        Long doctorId = Long.valueOf(principal.getName());
        Doctor currentDoctor = doctorRepo.findById(doctorId).orElseThrow(() ->
                new ResourceNotFoundException("Doctor not found with the id " + doctorId));

        if(!passwordEncoder.matches(OldPassword, currentDoctor.getPassword())) {
            throw new RuntimeException("Old Password is incorrect");
        }

        currentDoctor.setPassword(passwordEncoder.encode(NewPassword));
        doctorRepo.insert(currentDoctor);
    }
}
