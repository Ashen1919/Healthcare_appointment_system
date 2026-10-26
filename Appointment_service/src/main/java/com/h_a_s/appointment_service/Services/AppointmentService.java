package com.h_a_s.appointment_service.Services;

import com.h_a_s.appointment_service.Dto.AppointmentCreationDto;
import com.h_a_s.appointment_service.Dto.AppointmentResponseDto;
import com.h_a_s.appointment_service.Dto.PageResponseDto;
import com.h_a_s.appointment_service.Exceptions.ResourceNotFoundException;
import com.h_a_s.appointment_service.Model.Appointment;
import com.h_a_s.appointment_service.Model.Status;
import com.h_a_s.appointment_service.Repository.AppointmentRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {
    private final AppointmentRepo appointmentRepo;
    private final ModelMapper modelMapper;
    private static final int SLOT_MINUTES = 30;

    // create appointment
    public AppointmentResponseDto createAppointment(AppointmentCreationDto dto, Principal principal) {
        Long currentUserId = Long.valueOf(principal.getName());

        LocalDateTime start = dto.getStartTime();
        if (start.getSecond() != 0 || start.getNano() != 0 || start.getMinute() % SLOT_MINUTES != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Start time must align to a " + SLOT_MINUTES + "-minute slot.");
        }

        Appointment appointment = Appointment.builder()
                .patientId(dto.getPatientId())
                .doctorId(dto.getDoctorId())
                .startTime(start)
                .endTime(start.plusMinutes(SLOT_MINUTES))
                .reason(dto.getReason())
                .createdBy(currentUserId)
                .build();

        try {
            Appointment saved = appointmentRepo.saveAndFlush(appointment);
            return modelMapper.map(saved, AppointmentResponseDto.class);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This time slot is already booked.");
        }
    }

    // get all appointments
    public PageResponseDto<AppointmentResponseDto> getAllAppointments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return toPageResponse(appointmentRepo.findAll(pageable));
    }

    // get specific appointment
    public AppointmentResponseDto getAppointmentById(Long id) {
        return modelMapper.map(findAppointment(id), AppointmentResponseDto.class);
    }

    // get appointments of a patient
    public PageResponseDto<AppointmentResponseDto> getAppointmentsByPatient(Long patientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        return toPageResponse(appointmentRepo.findByPatientId(patientId, pageable));
    }

    // get appointments of a doctor
    public PageResponseDto<AppointmentResponseDto> getAppointmentsByDoctor(Long doctorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        return toPageResponse(appointmentRepo.findByDoctorId(doctorId, pageable));
    }

    // get booked (SCHEDULED) start times of a doctor in a range, used for availability calculation
    public List<LocalDateTime> getBookedStartTimes(Long doctorId, LocalDateTime from, LocalDateTime to) {
        return appointmentRepo
                .findByDoctorIdAndStatusAndStartTimeBetween(doctorId, Status.SCHEDULED, from, to)
                .stream()
                .map(Appointment::getStartTime)
                .toList();
    }

    // status changes
    public AppointmentResponseDto cancelAppointment(Long id) {
        return changeStatus(id, Status.CANCELLED);
    }

    public AppointmentResponseDto completeAppointment(Long id) {
        return changeStatus(id, Status.COMPLETED);
    }

    public AppointmentResponseDto markNoShow(Long id) {
        return changeStatus(id, Status.NO_SHOW);
    }

    private AppointmentResponseDto changeStatus(Long id, Status newStatus) {
        Appointment appointment = findAppointment(id);

        if (appointment.getStatus() != Status.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only SCHEDULED appointments can be changed. Current status: " + appointment.getStatus());
        }

        appointment.setStatus(newStatus);
        Appointment updated = appointmentRepo.save(appointment);
        return modelMapper.map(updated, AppointmentResponseDto.class);
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepo.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Appointment can't find with id: " + id));
    }

    private PageResponseDto<AppointmentResponseDto> toPageResponse(Page<Appointment> appointments) {
        List<AppointmentResponseDto> content = appointments.getContent()
                .stream()
                .map(appointment -> modelMapper.map(appointment, AppointmentResponseDto.class))
                .toList();

        return new PageResponseDto<>(
                content,
                appointments.getNumber(),
                appointments.getSize(),
                appointments.getTotalElements(),
                appointments.getTotalPages(),
                appointments.isLast()
        );
    }
}
