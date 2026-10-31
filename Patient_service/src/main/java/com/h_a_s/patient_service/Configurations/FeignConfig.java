package com.h_a_s.patient_service.Configurations;

import com.h_a_s.patient_service.Exceptions.ResourceNotFoundException;
import feign.*;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignConfig {

    // Global timeout
    @Bean
    public Request.Options requestOption() {
        return new Request.Options(
                3000L, TimeUnit.MILLISECONDS,
                5000L, TimeUnit.MILLISECONDS,
                true
        );
    }

    // Retry policy
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100L, 1000L, 3);
    }

    // Logger level
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    // Custom error decoder
    @Bean
    public ErrorDecoder customErrorDecoder() {
        return new ErrorDecoder() {
            private final ErrorDecoder defaultDecoder = new Default();
            @Override
            public Exception decode(String methodKey, Response response) {
                HttpStatus status = HttpStatus.valueOf(response.status());
                if (status == HttpStatus.NOT_FOUND) {
                    return new ResourceNotFoundException("Requested resource was not found on remote service [" + methodKey + "]");
                }
                if (status == HttpStatus.SERVICE_UNAVAILABLE || status == HttpStatus.GATEWAY_TIMEOUT) {
                    // Triggers the Feign Retryer!
                    return new RetryableException(
                            response.status(),
                            "Remote service temporarily unavailable: " + response.reason(),
                            response.request().httpMethod(),
                            (Long) null,
                            response.request()
                    );
                }
                if (status.is4xxClientError()) {
                    return new ResponseStatusException(status, "Client error from remote service: " + response.reason());
                }
                if (status.is5xxServerError()) {
                    return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Remote service failure: " + response.reason());
                }
                return defaultDecoder.decode(methodKey, response);
            }
        };
    }
}
