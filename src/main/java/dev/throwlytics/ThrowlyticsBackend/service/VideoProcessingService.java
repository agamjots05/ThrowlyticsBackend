package dev.throwlytics.ThrowlyticsBackend.service;

import dev.throwlytics.ThrowlyticsBackend.dto.ReleaseDetectionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service for communicating with Python video processing service
 */
@Service
public class VideoProcessingService {
    
    @Value("${python.service.url:http://localhost:8000}")
    private String pythonServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public VideoProcessingService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Process video using Python service to detect discus release
     * 
     * @param videoFile Video file to process
     * @param distanceThreshold Distance threshold for release detection (default: 180)
     * @param minVisibleFrames Minimum visible frames needed (default: 5)
     * @param frameSkip Process every Nth frame (default: 3)
     * @return ReleaseDetectionResponse with detection results
     * @throws IOException if file handling fails
     * @throws RestClientException if API call fails
     */
    public ReleaseDetectionResponse processVideo(
            MultipartFile videoFile,
            Integer distanceThreshold,
            Integer minVisibleFrames,
            Integer frameSkip
    ) throws IOException, RestClientException {
        
        // Create temporary file for video
        Path tempFile = Files.createTempFile("video_", ".mp4");
        try {
            // Copy multipart file to temporary file
            videoFile.transferTo(tempFile.toFile());
            
            // Prepare multipart request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tempFile.toFile()));
            
            // Add optional parameters if provided
            if (distanceThreshold != null) {
                body.add("distance_threshold", distanceThreshold);
            }
            if (minVisibleFrames != null) {
                body.add("min_visible_frames", minVisibleFrames);
            }
            if (frameSkip != null) {
                body.add("frame_skip", frameSkip);
            }
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Call Python service
            String url = pythonServiceUrl + "/api/video/process";
            ResponseEntity<ReleaseDetectionResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    ReleaseDetectionResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RestClientException("Python service returned error: " + response.getStatusCode());
            }
            
        } finally {
            // Cleanup temporary file
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                // Log but don't throw - cleanup failure shouldn't break the flow
                System.err.println("Failed to delete temporary file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Process video with default parameters
     */
    public ReleaseDetectionResponse processVideo(MultipartFile videoFile) throws IOException, RestClientException {
        return processVideo(videoFile, null, null, null);
    }
    
    /**
     * Process video from a stored file path (used after file is already saved)
     * 
     * @param videoFilePath Path to the stored video file
     * @param distanceThreshold Distance threshold for release detection (default: 180)
     * @param minVisibleFrames Minimum visible frames needed (default: 5)
     * @param frameSkip Process every Nth frame (default: 3)
     * @return ReleaseDetectionResponse with detection results
     * @throws IOException if file handling fails
     * @throws RestClientException if API call fails
     */
    public ReleaseDetectionResponse processVideoFromFile(
            Path videoFilePath,
            Integer distanceThreshold,
            Integer minVisibleFrames,
            Integer frameSkip
    ) throws IOException, RestClientException {
        
        // Verify file exists
        if (!Files.exists(videoFilePath)) {
            throw new IOException("Video file not found: " + videoFilePath);
        }
        
        // Prepare multipart request using the stored file
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(videoFilePath.toFile()));
        
        // Add optional parameters if provided
        if (distanceThreshold != null) {
            body.add("distance_threshold", distanceThreshold);
        }
        if (minVisibleFrames != null) {
            body.add("min_visible_frames", minVisibleFrames);
        }
        if (frameSkip != null) {
            body.add("frame_skip", frameSkip);
        }
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        // Call Python service
        String url = pythonServiceUrl + "/api/video/process";
        ResponseEntity<ReleaseDetectionResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                ReleaseDetectionResponse.class
        );
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RestClientException("Python service returned error: " + response.getStatusCode());
        }
    }
    
    /**
     * Process video from a stored file path with default parameters
     */
    public ReleaseDetectionResponse processVideoFromFile(Path videoFilePath) throws IOException, RestClientException {
        return processVideoFromFile(videoFilePath, null, null, null);
    }
    
    /**
     * Health check for Python service
     * @return true if service is healthy, false otherwise
     */
    public boolean isServiceHealthy() {
        try {
            String url = pythonServiceUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}

