package dev.throwlytics.ThrowlyticsBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private String message;
    private LocalDateTime timestamp;
    private List<String> details;
    
    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(String message, List<String> details) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }
}

