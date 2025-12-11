package dev.throwlytics.ThrowlyticsBackend.service;

import dev.throwlytics.ThrowlyticsBackend.exception.InvalidVideoFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * Service for validating video file uploads
 */
@Service
public class VideoValidationService {
    
    @Value("${spring.servlet.multipart.max-file-size:500MB}")
    private String maxFileSize;
    
    // Allowed video MIME types
    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
        "video/mp4",
        "video/quicktime",  // MOV
        "video/x-msvideo",  // AVI
        "video/x-matroska", // MKV
        "video/webm"
    );
    
    // Allowed file extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".mp4", ".mov", ".avi", ".mkv", ".webm"
    );
    
    /**
     * Validate video file
     * 
     * @param file Multipart file to validate
     * @throws InvalidVideoFileException if validation fails
     */
    public void validateVideoFile(MultipartFile file) {
        if (file == null) {
            throw new InvalidVideoFileException("File is required");
        }
        
        if (file.isEmpty()) {
            throw new InvalidVideoFileException("File is empty");
        }
        
        // Check file size (500MB default)
        long maxSizeBytes = parseSize(maxFileSize);
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidVideoFileException(
                String.format("File size exceeds maximum allowed size of %s", maxFileSize)
            );
        }
        
        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new InvalidVideoFileException("File must be a video");
        }
        
        // Check if content type is in allowed list
        if (!ALLOWED_VIDEO_TYPES.contains(contentType)) {
            throw new InvalidVideoFileException(
                String.format("Video type '%s' is not supported. Allowed types: %s", 
                    contentType, String.join(", ", ALLOWED_VIDEO_TYPES))
            );
        }
        
        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new InvalidVideoFileException(
                    String.format("File extension '%s' is not supported. Allowed extensions: %s",
                        extension, String.join(", ", ALLOWED_EXTENSIONS))
                );
            }
        }
    }
    
    /**
     * Parse size string (e.g., "500MB") to bytes
     */
    private long parseSize(String size) {
        if (size == null || size.isEmpty()) {
            return 500 * 1024 * 1024; // Default 500MB
        }
        
        size = size.trim().toUpperCase();
        long multiplier = 1;
        
        if (size.endsWith("KB")) {
            multiplier = 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("MB")) {
            multiplier = 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        }
        
        try {
            return Long.parseLong(size.trim()) * multiplier;
        } catch (NumberFormatException e) {
            return 500 * 1024 * 1024; // Default 500MB
        }
    }
}

