package dev.throwlytics.ThrowlyticsBackend.service;

import dev.throwlytics.ThrowlyticsBackend.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service for handling file storage operations
 */
@Service
public class FileStorageService {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.upload.videos:uploads/videos}")
    private String videosDir;
    
    @Value("${app.upload.thumbnails:uploads/thumbnails}")
    private String thumbnailsDir;
    
    /**
     * Store uploaded video file
     * @param file Multipart file
     * @param userId User ID for organizing files
     * @return Path to stored file (relative to uploads directory)
     * @throws FileStorageException if file storage fails
     */
    public String storeVideo(MultipartFile file, Long userId) {
        Path filePath = null;
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                throw new FileStorageException("File is null or empty");
            }
            
            // Create user-specific directory
            Path userVideoDir = Paths.get(videosDir, userId.toString());
            Files.createDirectories(userVideoDir);
            
            // Verify directory was created and is writable
            if (!Files.exists(userVideoDir)) {
                throw new FileStorageException("Failed to create video directory: " + userVideoDir.toAbsolutePath());
            }
            if (!Files.isWritable(userVideoDir)) {
                throw new FileStorageException("Video directory is not writable: " + userVideoDir.toAbsolutePath());
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".mp4";
            String filename = UUID.randomUUID().toString() + extension;
            
            // Store file
            filePath = userVideoDir.resolve(filename);
            
            // Verify the multipart file is readable before attempting copy
            if (!file.getResource().isReadable()) {
                throw new FileStorageException("Multipart file is not readable. File may have been deleted or corrupted.");
            }
            
            // Verify we can get an input stream
            try {
                file.getInputStream().close(); // Test if we can open the stream
            } catch (IOException e) {
                throw new FileStorageException("Cannot read from uploaded file. File may be corrupted or already deleted.", e);
            }
            
            // Check available disk space (optional, but helpful)
            long fileSize = file.getSize();
            long availableSpace = userVideoDir.toFile().getUsableSpace();
            if (fileSize > availableSpace) {
                throw new FileStorageException(
                    String.format("Insufficient disk space. Required: %d bytes, Available: %d bytes", 
                        fileSize, availableSpace)
                );
            }
            
            // Copy file using buffered stream for better performance with large files
            // This handles both in-memory and disk-based multipart files
            try (var inputStream = file.getInputStream();
                 var outputStream = Files.newOutputStream(filePath)) {
                
                // Use a larger buffer (64KB) for better performance with large files
                byte[] buffer = new byte[65536]; // 64KB buffer
                long totalBytesRead = 0;
                int bytesRead;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
                
                outputStream.flush();
                
                // Verify we read the expected amount
                if (totalBytesRead != fileSize) {
                    throw new IOException(
                        String.format("File size mismatch during copy. Expected: %d bytes, Read: %d bytes", 
                            fileSize, totalBytesRead)
                    );
                }
            }
            
            // Verify file was written
            if (!Files.exists(filePath)) {
                throw new FileStorageException("File was not created: " + filePath.toAbsolutePath());
            }
            
            long writtenSize = Files.size(filePath);
            if (writtenSize == 0) {
                Files.deleteIfExists(filePath);
                throw new FileStorageException(
                    String.format("File was written but is empty. Expected: %d bytes, Written: %d bytes", 
                        fileSize, writtenSize)
                );
            }
            
            if (writtenSize != fileSize) {
                // Log warning but don't fail - file might be partially written
                System.err.println(String.format(
                    "Warning: File size mismatch. Expected: %d bytes, Written: %d bytes", 
                    fileSize, writtenSize
                ));
            }
            
            // Return relative path for URL generation
            return Paths.get("videos", userId.toString(), filename).toString().replace("\\", "/");
        } catch (IOException e) {
            // Clean up partial file if it exists
            if (filePath != null) {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException cleanupEx) {
                    // Ignore cleanup errors
                }
            }
            
            // Get more detailed error information
            String errorMessage = e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " (Caused by: " + e.getCause().getMessage() + ")";
            }
            
            String errorDetails = String.format(
                "Failed to store video file. Directory: %s, Target: %s, File size: %d bytes (%.2f MB), Error: %s", 
                videosDir, 
                filePath != null ? filePath.toAbsolutePath() : "unknown",
                file != null ? file.getSize() : 0,
                file != null ? file.getSize() / (1024.0 * 1024.0) : 0.0,
                errorMessage
            );
            
            // Log the full stack trace for debugging
            System.err.println(errorDetails);
            System.err.println("Exception type: " + e.getClass().getName());
            e.printStackTrace();
            
            throw new FileStorageException(errorDetails, e);
        } catch (Exception e) {
            // Clean up partial file if it exists
            if (filePath != null) {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException cleanupEx) {
                    // Ignore cleanup errors
                }
            }
            
            throw new FileStorageException(
                String.format("Unexpected error storing video file: %s", e.getMessage()), 
                e
            );
        }
    }
    
    /**
     * Store thumbnail image
     * @param thumbnailBytes Thumbnail image bytes
     * @param userId User ID
     * @return Path to stored thumbnail
     * @throws FileStorageException if storage fails
     */
    public String storeThumbnail(byte[] thumbnailBytes, Long userId) {
        try {
            // Create user-specific directory
            Path userThumbnailDir = Paths.get(thumbnailsDir, userId.toString());
            Files.createDirectories(userThumbnailDir);
            
            // Generate unique filename
            String filename = UUID.randomUUID().toString() + ".jpg";
            
            // Store file
            Path filePath = userThumbnailDir.resolve(filename);
            Files.write(filePath, thumbnailBytes);
            
            // Return relative path
            return Paths.get("thumbnails", userId.toString(), filename).toString().replace("\\", "/");
        } catch (IOException e) {
            throw new FileStorageException("Failed to store thumbnail: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get full path to video file
     * @param relativePath Relative path from storeVideo
     * @return Full path to file
     */
    public Path getVideoPath(String relativePath) {
        return Paths.get(videosDir).resolve(relativePath.replace("videos/", ""));
    }
    
    /**
     * Initialize upload directories
     */
    public void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(videosDir));
            Files.createDirectories(Paths.get(thumbnailsDir));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directories", e);
        }
    }
}

