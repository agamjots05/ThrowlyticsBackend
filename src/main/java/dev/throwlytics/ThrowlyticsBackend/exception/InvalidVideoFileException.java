package dev.throwlytics.ThrowlyticsBackend.exception;

/**
 * Exception thrown when video file validation fails
 */
public class InvalidVideoFileException extends RuntimeException {
    
    public InvalidVideoFileException(String message) {
        super(message);
    }
    
    public InvalidVideoFileException(String message, Throwable cause) {
        super(message, cause);
    }
}

