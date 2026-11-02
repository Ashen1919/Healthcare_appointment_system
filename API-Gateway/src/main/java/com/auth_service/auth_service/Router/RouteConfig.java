package com.auth_service.auth_service.Router;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;

@Configuration
public class RouteConfig {

    @Value("${doctor.service.url}")
    private String doctorServiceUrl;

    @Value("${patient.service.url}")
    private String patientServiceUrl;

    @Value("${appointment.service.url}")
    private String appointmentServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> AppointmentServiceRoute() {
        return GatewayRouterFunctions.route("appointment-service")
                .route(RequestPredicates.path("/api/v1/appointments"), HandlerFunctions.http())
                .before(uri(appointmentServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> DoctorServiceRoute() {
        return GatewayRouterFunctions.route("doctor-service")
                .route(RequestPredicates.path("/api/v1/doctors"), HandlerFunctions.http())
                .before(uri(doctorServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> PatientServiceRoute() {
        return GatewayRouterFunctions.route("patient-service")
                .route(RequestPredicates.path("/api/v1/patients"), HandlerFunctions.http())
                .before(uri(patientServiceUrl))
                .build();
    }
}
