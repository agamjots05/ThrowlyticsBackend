package dev.throwlytics.ThrowlyticsBackend.service;

import dev.throwlytics.ThrowlyticsBackend.dto.ReleaseDetectionResponse;
import dev.throwlytics.ThrowlyticsBackend.exception.ThumbnailGenerationException;
import dev.throwlytics.ThrowlyticsBackend.model.ThowHistory;
import dev.throwlytics.ThrowlyticsBackend.model.User;
import dev.throwlytics.ThrowlyticsBackend.repository.ThrowHistoryRepository;
import dev.throwlytics.ThrowlyticsBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing throw history
 */
@Service
public class ThrowHistoryService {
    
    @Autowired
    private ThrowHistoryRepository throwHistoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private ThumbnailService thumbnailService;
    
    /**
     * Save a throw history entry with video processing results
     * 
     * @param userId User ID
     * @param videoPath Relative path to video file
     * @param processingResult Processing results from Python service (can be null)
     * @return Saved ThrowHistory entity
     * @throws ThumbnailGenerationException if thumbnail generation fails
     */
    @Transactional
    public ThowHistory saveThrowHistory(
            Long userId,
            String videoPath,
            ReleaseDetectionResponse processingResult
    ) {
        
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Create new throw history entry
        ThowHistory throwHistory = new ThowHistory();
        throwHistory.setUser(user);
        throwHistory.setVideoUrl(videoPath);
        throwHistory.setUploadDate(LocalDateTime.now());
        
        // Set processing results if available
        if (processingResult != null) {
            throwHistory.setReleaseFrame(processingResult.getReleaseFrame());
            throwHistory.setReleaseConfirmed(processingResult.getReleaseConfirmed());
            throwHistory.setTotalFrames(processingResult.getTotalFrames());
            throwHistory.setVideoWidth(processingResult.getVideoWidth());
            throwHistory.setVideoHeight(processingResult.getVideoHeight());
            throwHistory.setFps(processingResult.getFps());
            throwHistory.setReleaseAngleDeg(processingResult.getReleaseAngleDeg());
            throwHistory.setReleaseLateralOffsetNorm(processingResult.getReleaseLateralOffsetNorm());
            throwHistory.setElbowAngleDeg(processingResult.getElbowAngleDeg());
            throwHistory.setShoulderAngleDeg(processingResult.getShoulderAngleDeg());
            throwHistory.setWristAngleDeg(processingResult.getWristAngleDeg());
        }
        
        // Generate thumbnail
        String thumbnailPath = generateThumbnail(videoPath, userId, processingResult);
        throwHistory.setThumbnailUrl(thumbnailPath);
        
        // Save to database
        return throwHistoryRepository.save(throwHistory);
    }
    
    /**
     * Get throw history for a user, ordered by most recent first
     * 
     * @param userId User ID
     * @return List of throw history entries
     */
    public List<ThowHistory> getThrowHistoryByUserId(Long userId) {
        return throwHistoryRepository.findByUserUserIdOrderByUploadDateDesc(userId);
    }
    
    /**
     * Generate thumbnail from video
     * Uses release frame if available, otherwise uses first frame
     * 
     * @param videoPath Relative path to video
     * @param userId User ID
     * @param processingResult Processing results (to get release frame)
     * @return Relative path to thumbnail
     * @throws ThumbnailGenerationException if thumbnail generation fails
     */
    private String generateThumbnail(
            String videoPath,
            Long userId,
            ReleaseDetectionResponse processingResult
    ) {
        try {
            // Get full path to video
            Path fullVideoPath = fileStorageService.getVideoPath(videoPath);
            
            // Determine which frame to extract
            // Priority: release frame if found, otherwise first frame (frame 0)
            int frameToExtract = 0;  // Default to first frame
            
            if (processingResult != null && 
                processingResult.getReleaseFrame() != null && 
                processingResult.getReleaseFrame() >= 0) {
                // Use release frame if available
                frameToExtract = processingResult.getReleaseFrame();
            }
            
            // Try to extract the selected frame
            byte[] thumbnailBytes;
            try {
                thumbnailBytes = thumbnailService.extractFrame(fullVideoPath, frameToExtract);
            } catch (IOException e) {
                // If release frame extraction fails, fall back to first frame
                if (frameToExtract != 0) {
                    System.err.println("Failed to extract release frame " + frameToExtract + 
                        ", falling back to first frame: " + e.getMessage());
                    thumbnailBytes = thumbnailService.extractFrame(fullVideoPath, 0);
                } else {
                    // If first frame extraction also fails, rethrow
                    throw e;
                }
            }
            
            // Store thumbnail
            return fileStorageService.storeThumbnail(thumbnailBytes, userId);
        } catch (IOException e) {
            throw new ThumbnailGenerationException("Failed to generate thumbnail: " + e.getMessage(), e);
        }
    }
}

