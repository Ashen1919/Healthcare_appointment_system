package com.h_a_s.appointment_service.Services;

import com.h_a_s.appointment_service.Client.DoctorClient;
import com.h_a_s.appointment_service.Client.PatientClient;
import com.h_a_s.appointment_service.Dto.AppointmentCreationDto;
import com.h_a_s.appointment_service.Dto.AppointmentResponseDto;
import com.h_a_s.appointment_service.Dto.Client.DoctorClientDto;
import com.h_a_s.appointment_service.Dto.Client.PatientClientDto;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {
    private final AppointmentRepo appointmentRepo;
    private final ModelMapper modelMapper;
    private final DoctorClient doctorClient;
    private final PatientClient patientClient;
    private static final int SLOT_MINUTES = 30;

    // create appointment
    public AppointmentResponseDto createAppointment(AppointmentCreationDto dto, Principal principal) {
        Long currentUserId = Long.valueOf(principal.getName());

        LocalDateTime start = dto.getStartTime();
        if (start.getSecond() != 0 || start.getNano() != 0 || start.getMinute() % SLOT_MINUTES != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Start time must align to a " + SLOT_MINUTES + "-minute slot.");
        }

        LocalDateTime end = start.plusMinutes(SLOT_MINUTES);

        // Verify patient exist and active
        PatientClientDto patient = patientClient.getPatientById(dto.getPatientId());
        if(patient == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with id: " + dto.getPatientId());
        }
        if (!patient.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient account is inactive.");
        }

        // Verify Doctor Existence & Status
        DoctorClientDto doctor = doctorClient.getDoctorById(dto.getDoctorId());
        if (doctor == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with id: " + dto.getDoctorId());
        }
        if (!doctor.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is currently inactive.");
        }

        // Verify Doctor Availability against TimeOff
        validateDoctorTimeOff(doctor, start, end);

        // Verify Doctor Weekly Working Hours
        validateDoctorSchedule(doctor, start, end);

        Appointment appointment = Appointment.builder()
                .patientId(dto.getPatientId())
                .doctorId(dto.getDoctorId())
                .startTime(start)
                .endTime(end)
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

    // Cancel all SCHEDULED appointments for a doctor (e.g. when deactivated)
    public int cancelScheduledAppointmentsByDoctor(Long doctorId) {
        List<Appointment> scheduledAppointments = appointmentRepo.findByDoctorIdAndStatus(doctorId, Status.SCHEDULED);
        for (Appointment appointment : scheduledAppointments) {
            appointment.setStatus(Status.CANCELLED);
        }
        appointmentRepo.saveAll(scheduledAppointments);
        return scheduledAppointments.size();
    }

    // Cancel all SCHEDULED appointments for a patient (e.g. when deactivated)
    public int cancelScheduledAppointmentsByPatient(Long patientId) {
        List<Appointment> scheduledAppointments = appointmentRepo.findByPatientIdAndStatus(patientId, Status.SCHEDULED);
        for (Appointment appointment : scheduledAppointments) {
            appointment.setStatus(Status.CANCELLED);
        }
        appointmentRepo.saveAll(scheduledAppointments);
        return scheduledAppointments.size();
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

    // Checks if the appointment overlaps with any Doctor's TimeOff period
    private void validateDoctorTimeOff(DoctorClientDto doctor, LocalDateTime start, LocalDateTime end) {
        if (doctor.getTimeOff() == null || doctor.getTimeOff().isEmpty()) {
            return;
        }
        Instant appStartInstant = start.atZone(ZoneId.systemDefault()).toInstant();
        Instant appEndInstant = end.atZone(ZoneId.systemDefault()).toInstant();
        boolean hasTimeOffConflict = doctor.getTimeOff().stream().anyMatch(timeOff ->
                timeOff.getFrom() != null && timeOff.getTo() != null &&
                        appStartInstant.isBefore(timeOff.getTo()) && appEndInstant.isAfter(timeOff.getFrom())
        );
        if (hasTimeOffConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Doctor is on leave/time-off during this requested time slot.");
        }
    }

    // Checks if the requested appointment falls within doctor's configured weekly schedule slots
    private void validateDoctorSchedule(DoctorClientDto doctor, LocalDateTime start, LocalDateTime end) {
        if (doctor.getSchedule() == null || doctor.getSchedule().isEmpty()) {
            return; // Doctor has no restricted schedule configured
        }
        boolean fitsInSchedule = doctor.getSchedule().stream().anyMatch(slot ->
                slot.getDayOfWeek() == start.getDayOfWeek() &&
                        !start.toLocalTime().isBefore(slot.getStart()) &&
                        !end.toLocalTime().isAfter(slot.getEnd())
        );
        if (!fitsInSchedule) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Doctor does not have available working hours for " + start.getDayOfWeek() +
                            " at " + start.toLocalTime());
        }
    }
}
