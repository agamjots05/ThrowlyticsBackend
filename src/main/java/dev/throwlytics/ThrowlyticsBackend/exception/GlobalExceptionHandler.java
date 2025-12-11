package dev.throwlytics.ThrowlyticsBackend.exception;

import dev.throwlytics.ThrowlyticsBackend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for consistent error responses
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handle validation errors (e.g., @Valid on SignupRequest)
     * Triggered when @NotBlank, @Email, @Size validations fail
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> details = new ArrayList<>();
        
        // Extract all validation error messages
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "Validation failed",
            LocalDateTime.now(),
            details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle invalid video file exceptions
     */
    @ExceptionHandler(InvalidVideoFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVideoFile(InvalidVideoFileException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            LocalDateTime.now(),
            null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle video processing exceptions
     */
    @ExceptionHandler(VideoProcessingException.class)
    public ResponseEntity<ErrorResponse> handleVideoProcessingException(VideoProcessingException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            LocalDateTime.now(),
            null
        );
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
    
    /**
     * Handle file storage exceptions
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(FileStorageException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            LocalDateTime.now(),
            null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Handle thumbnail generation exceptions
     */
    @ExceptionHandler(ThumbnailGenerationException.class)
    public ResponseEntity<ErrorResponse> handleThumbnailGenerationException(ThumbnailGenerationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            LocalDateTime.now(),
            null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Handle runtime exceptions (e.g., "Email already exists")
     * This should be last among specific exception handlers
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            LocalDateTime.now(),
            null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "An unexpected error occurred",
            LocalDateTime.now(),
            List.of(ex.getMessage())
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
