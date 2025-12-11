package dev.throwlytics.ThrowlyticsBackend.dto;

import dev.throwlytics.ThrowlyticsBackend.model.PlanType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String name;
    private String email;
    private PlanType planType;
    private int monthlyTokenLimit;
    private String token;
}

