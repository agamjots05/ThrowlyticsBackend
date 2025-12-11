package dev.throwlytics.ThrowlyticsBackend.exception;

/**
 * Exception thrown when video processing fails
 */
public class VideoProcessingException extends RuntimeException {
    
    public VideoProcessingException(String message) {
        super(message);
    }
    
    public VideoProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

