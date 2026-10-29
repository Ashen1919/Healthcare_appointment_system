package com.h_a_s.appointment_service.Client;

@org.springframework.cloud.openfeign.FeignClient(name = "doctor-service", url = "${doctor.service.url}")
public interface DoctorClient {
}
