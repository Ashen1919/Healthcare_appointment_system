package com.h_a_s.doctor_service.Services;

import com.h_a_s.doctor_service.Client.AppointmentClient;
import com.h_a_s.doctor_service.Dto.DoctorCreateRequestDto;
import com.h_a_s.doctor_service.Dto.DoctorResponseDto;
import com.h_a_s.doctor_service.Dto.DoctorUpdateRequestDto;
import com.h_a_s.doctor_service.Dto.PageResponseDto;
import com.h_a_s.doctor_service.Dto.TimeOffRequestDto;
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

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorService {
    private final DoctorRepo doctorRepo;
    private final ModelMapper modelMapper;
    private final DoctorIdGenerator doctorIdGenerator;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentClient appointmentClient;

    // Create a doctor
    public DoctorResponseDto createDoctor(DoctorCreateRequestDto requestDto) {
        if (doctorRepo.existsByEmail(requestDto.getEmail())) {
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

        if (doctorUpdateRequestDto.getTimeOff() != null) {
            // Validate all time-off entries against scheduled appointments in Appointment Service
            for (Doctor.TimeOff timeOff : doctorUpdateRequestDto.getTimeOff()) {
                validateTimeOffAgainstScheduledAppointments(doctorId, timeOff.getFrom(), timeOff.getTo());
            }
            currentDoctor.setTimeOff(doctorUpdateRequestDto.getTimeOff());
        }

        Doctor saveDoctor = doctorRepo.save(currentDoctor);
        return modelMapper.map(saveDoctor, DoctorResponseDto.class);
    }

    // Add time-off for a doctor with validation against scheduled appointments
    public DoctorResponseDto addTimeOff(Long doctorId, TimeOffRequestDto requestDto, Principal principal) {
        if (principal != null && principal.getName() != null) {
            Long currentDoctorId = Long.valueOf(principal.getName());
            if (!currentDoctorId.equals(doctorId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not authorized to add time-off for another doctor.");
            }
        }

        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow(() ->
                new ResourceNotFoundException("Doctor not found with the id " + doctorId));

        // Validate time-off range against active SCHEDULED appointments via OpenFeign
        validateTimeOffAgainstScheduledAppointments(doctorId, requestDto.getFrom(), requestDto.getTo());

        if (doctor.getTimeOff() == null) {
            doctor.setTimeOff(new ArrayList<>());
        }
        doctor.getTimeOff().add(new Doctor.TimeOff(requestDto.getFrom(), requestDto.getTo(), requestDto.getReason()));

        Doctor saved = doctorRepo.save(doctor);
        return modelMapper.map(saved, DoctorResponseDto.class);
    }

    // Deactivate doctor and cancel all their SCHEDULED appointments in Appointment Service
    public DoctorResponseDto deactivateDoctor(Long id, Principal principal) {
        if (principal != null && principal.getName() != null) {
            Long currentDoctorId = Long.valueOf(principal.getName());
            // Doctor deactivating themselves, or administrative action
            if (!currentDoctorId.equals(id)) {
                // If needed, check admin role, or permit admin
            }
        }

        Doctor doctor = doctorRepo.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Doctor not found with the id " + id));

        if (doctor.isActive()) {
            doctor.setActive(false);

            // Call Appointment Service to cancel all future scheduled appointments
            appointmentClient.cancelScheduledAppointmentsByDoctor(id);

            doctor = doctorRepo.save(doctor);
        }

        return modelMapper.map(doctor, DoctorResponseDto.class);
    }

    // Activate a doctor
    public DoctorResponseDto activateDoctor(Long id) {
        Doctor doctor = doctorRepo.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Doctor not found with the id " + id));

        doctor.setActive(true);
        Doctor saved = doctorRepo.save(doctor);
        return modelMapper.map(saved, DoctorResponseDto.class);
    }

    // update doctor's password
    public void updateDoctorPassword(String OldPassword, String NewPassword, Principal principal) {
        Long doctorId = Long.valueOf(principal.getName());
        Doctor currentDoctor = doctorRepo.findById(doctorId).orElseThrow(() ->
                new ResourceNotFoundException("Doctor not found with the id " + doctorId));

        if (!passwordEncoder.matches(OldPassword, currentDoctor.getPassword())) {
            throw new RuntimeException("Old Password is incorrect");
        }

        currentDoctor.setPassword(passwordEncoder.encode(NewPassword));
        doctorRepo.save(currentDoctor);
    }

    /**
     * Checks if doctor has any active SCHEDULED appointments in the requested time-off range.
     * Throws 409 Conflict if appointments exist in that period.
     */
    private void validateTimeOffAgainstScheduledAppointments(Long doctorId, Instant from, Instant to) {
        if (from == null || to == null) {
            return;
        }
        if (!from.isBefore(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Time-off start time ('from') must be before end time ('to').");
        }

        LocalDateTime fromLdt = LocalDateTime.ofInstant(from, ZoneId.systemDefault());
        LocalDateTime toLdt = LocalDateTime.ofInstant(to, ZoneId.systemDefault());

        List<LocalDateTime> bookedAppointments = appointmentClient.getBookedStartTimes(doctorId, fromLdt, toLdt);
        if (bookedAppointments != null && !bookedAppointments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot take time-off. Doctor has " + bookedAppointments.size() +
                            " scheduled appointment(s) during this period. Please reschedule or cancel them first.");
        }
    }
}
