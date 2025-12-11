package dev.throwlytics.ThrowlyticsBackend.controller;

import dev.throwlytics.ThrowlyticsBackend.dto.ReleaseDetectionResponse;
import dev.throwlytics.ThrowlyticsBackend.dto.VideoUploadResponse;
import dev.throwlytics.ThrowlyticsBackend.model.ThowHistory;
import dev.throwlytics.ThrowlyticsBackend.service.FileStorageService;
import dev.throwlytics.ThrowlyticsBackend.service.ThrowHistoryService;
import dev.throwlytics.ThrowlyticsBackend.service.VideoProcessingService;
import dev.throwlytics.ThrowlyticsBackend.service.VideoValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for video upload and processing endpoints
 */
@RestController
@RequestMapping("/api/video")
public class VideoController {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private VideoProcessingService videoProcessingService;
    
    @Autowired
    private ThrowHistoryService throwHistoryService;
    
    @Autowired
    private VideoValidationService videoValidationService;
    
    /**
     * Upload video endpoint
     * POST /api/video/upload
     * 
     * Headers:
     *   Authorization: Bearer <JWT_TOKEN>
     *   Content-Type: multipart/form-data
     * 
     * Body:
     *   file: Video file (MP4, MOV, AVI, etc.)
     * 
     * Success response (200 OK):
     * {
     *   "throwId": 1,
     *   "userId": 1,
     *   "releaseFrame": 42,
     *   "releaseConfirmed": true,
     *   "totalFrames": 1361,
     *   "videoWidth": 1080,
     *   "videoHeight": 1350,
     *   "fps": 60,
     *   "videoUrl": "videos/1/uuid.mp4",
     *   "thumbnailUrl": "thumbnails/1/uuid.jpg",
     *   "uploadDate": "2025-01-07T10:30:00",
     *   "message": "Video processed successfully"
     * }
     */
    @PostMapping("/upload")
    public ResponseEntity<VideoUploadResponse> uploadVideo(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        // Validate file (throws InvalidVideoFileException if invalid)
        videoValidationService.validateVideoFile(file);
        
        // Get user ID from authentication
        Long userId = (Long) authentication.getPrincipal();
        
        // IMPORTANT: Store video file FIRST before processing
        // This ensures the file is saved before any processing that might consume the stream
        // Throws FileStorageException if storage fails
        String videoPath = fileStorageService.storeVideo(file, userId);
        
        // Phase 3: Process video using Python service
        // Now we can process from the stored file location
        ReleaseDetectionResponse processingResult = null;
        String errorMessage = null;
        
        try {
            // Get the full path to the stored video file
            java.nio.file.Path storedVideoPath = fileStorageService.getVideoPath(videoPath);
            processingResult = videoProcessingService.processVideoFromFile(storedVideoPath);
        } catch (RestClientException e) {
            // Python service unavailable or error
            errorMessage = "Video processing service unavailable: " + e.getMessage();
            // Continue - we'll still keep the video
        } catch (Exception e) {
            errorMessage = "Error processing video: " + e.getMessage();
        }
        
        // Phase 4: Save to ThrowHistory and generate thumbnail
        // Throws ThumbnailGenerationException if thumbnail generation fails
        ThowHistory throwHistory = throwHistoryService.saveThrowHistory(userId, videoPath, processingResult);
        
        // Build response
        VideoUploadResponse response = new VideoUploadResponse();
        response.setUserId(userId);
        response.setVideoUrl(videoPath);
        response.setThrowId(throwHistory.getThrowId());
        response.setUploadDate(throwHistory.getUploadDate());
        response.setThumbnailUrl(throwHistory.getThumbnailUrl());
        response.setReleaseAngleDeg(throwHistory.getReleaseAngleDeg());
        response.setReleaseLateralOffsetNorm(throwHistory.getReleaseLateralOffsetNorm());
        response.setElbowAngleDeg(throwHistory.getElbowAngleDeg());
        response.setShoulderAngleDeg(throwHistory.getShoulderAngleDeg());
        response.setWristAngleDeg(throwHistory.getWristAngleDeg());
        
        if (processingResult != null) {
            // Processing succeeded
            response.setReleaseFrame(processingResult.getReleaseFrame());
            response.setReleaseConfirmed(processingResult.getReleaseConfirmed());
            response.setTotalFrames(processingResult.getTotalFrames());
            response.setVideoWidth(processingResult.getVideoWidth());
            response.setVideoHeight(processingResult.getVideoHeight());
            response.setFps(processingResult.getFps());
            response.setReleaseAngleDeg(processingResult.getReleaseAngleDeg());
            response.setReleaseLateralOffsetNorm(processingResult.getReleaseLateralOffsetNorm());
            response.setElbowAngleDeg(processingResult.getElbowAngleDeg());
            response.setShoulderAngleDeg(processingResult.getShoulderAngleDeg());
            response.setWristAngleDeg(processingResult.getWristAngleDeg());
            response.setMessage(processingResult.getMessage() != null 
                ? processingResult.getMessage() 
                : "Video processed successfully");
        } else {
            // Processing failed but video was stored
            response.setMessage("Video uploaded successfully, but processing failed. " + 
                (errorMessage != null ? errorMessage : "Please try again later."));
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get throw history for authenticated user
     * GET /api/video/history
     * 
     * Headers:
     *   Authorization: Bearer <JWT_TOKEN>
     * 
     * Success response (200 OK):
     * [
     *   {
     *     "throwId": 1,
     *     "releaseFrame": 42,
     *     "releaseConfirmed": true,
     *     "totalFrames": 1361,
     *     "videoWidth": 1080,
     *     "videoHeight": 1350,
     *     "fps": 60,
     *     "videoUrl": "videos/1/uuid.mp4",
     *     "thumbnailUrl": "thumbnails/1/uuid.jpg",
     *     "uploadDate": "2025-01-07T10:30:00"
     *   },
     *   ...
     * ]
     */
    @GetMapping("/history")
    public ResponseEntity<List<ThowHistory>> getThrowHistory(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<ThowHistory> history = throwHistoryService.getThrowHistoryByUserId(userId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Health check for video service
     * GET /api/video/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Video service is running");
    }
}

