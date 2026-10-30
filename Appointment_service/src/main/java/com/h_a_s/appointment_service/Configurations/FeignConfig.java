package com.h_a_s.appointment_service.Configurations;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
