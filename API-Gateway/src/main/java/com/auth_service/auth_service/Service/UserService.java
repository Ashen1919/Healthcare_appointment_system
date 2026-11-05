package com.auth_service.auth_service.Service;

import com.auth_service.auth_service.Client.DoctorClient;
import com.auth_service.auth_service.Client.PatientClient;
import com.auth_service.auth_service.Dto.*;
import com.auth_service.auth_service.Model.AuthModel;
import com.auth_service.auth_service.Model.Roles;
import com.auth_service.auth_service.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final PatientClient patientClient;
    private final DoctorClient doctorClient;

    // Create Patient with Saves Auth credentials & notifies Patient Service
    public UserResponseDto createPatient(PatientUserCreateRequestDto dto) {
        // Create auth credentials
        UserResponseDto authUser = registerUser(dto, Roles.PATIENT);

        // Prepare payload for Patient Service
        PatientClient.PatientRequestPayload patientPayload = new PatientClient.PatientRequestPayload();
        patientPayload.setEmail(dto.getEmail());
        patientPayload.setName(dto.getName());
        patientPayload.setDob(dto.getDob());
        patientPayload.setContact(dto.getContact());
        patientPayload.setAddress(dto.getAddress());

        // Feign call
        patientClient.createPatient(patientPayload);

        return authUser;
    }

    // Create Doctor with Saves Auth credentials & notifies Doctor Service
    public UserResponseDto createDoctor(DoctorUserCreateRequestDto dto) {
        // Create auth credentials
        UserResponseDto authUser = registerUser(dto, Roles.DOCTOR);

        // Prepare payload for Doctor Service
        DoctorClient.DoctorRequestPayload doctorPayload = new DoctorClient.DoctorRequestPayload();
        doctorPayload.setEmail(dto.getEmail());
        doctorPayload.setName(dto.getName());
        doctorPayload.setSpecialty(dto.getSpecialty());
        doctorPayload.setContact(dto.getContact());
        doctorPayload.setSchedule(dto.getSchedule());

        // Feign call
        doctorClient.createDoctor(doctorPayload);

        return authUser;
    }

    // Create Admin with Only requires Auth credentials
    public UserResponseDto createAdmin(UserCreateRequestDto dto) {
        return registerUser(dto, Roles.ADMIN);
    }

    // Helper method to persist Auth entity
    public UserResponseDto registerUser(UserCreateRequestDto userCreateRequestDto, Roles roles) {
        if (userRepository.existsByEmail(userCreateRequestDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists");
        }

        AuthModel authModel = modelMapper.map(userCreateRequestDto, AuthModel.class);
        authModel.setPassword(passwordEncoder.encode(userCreateRequestDto.getPassword()));
        authModel.setEnabled(true);
        authModel.set_password_changed(false);
        authModel.setRole(roles);

        AuthModel savedUser = userRepository.save(authModel);
        return modelMapper.map(savedUser, UserResponseDto.class);
    }

    // Update Password for currently logged-in user
    public UserResponseDto updatePassword(UpdatePasswordRequestDto dto, Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        // Validate that new password matches confirm password
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password and confirm password do not match");
        }

        // Find user by principal (supports either email or numeric user ID)
        String identifier = principal.getName();
        AuthModel user = userRepository.findByEmail(identifier)
                .or(() -> {
                    try {
                        return userRepository.findById(Long.valueOf(identifier));
                    } catch (NumberFormatException e) {
                        return Optional.empty();
                    }
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Verify old password
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password does not match");
        }

        // Prevent setting the exact same password
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be the same as old password");
        }

        // Update password and mark password changed flag
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.set_password_changed(true);

        AuthModel updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserResponseDto.class);
    }


}