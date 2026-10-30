package com.h_a_s.doctor_service.Dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeOffRequestDto {
    @NotNull(message = "Start time ('from') is required.")
    @Future(message = "Start time ('from') must be in the future.")
    private Instant from;

    @NotNull(message = "End time ('to') is required.")
    @Future(message = "End time ('to') must be in the future.")
    private Instant to;

    private String reason;
}
