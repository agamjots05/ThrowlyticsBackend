package dev.throwlytics.ThrowlyticsBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Python service response
 * Matches the ReleaseDetectionResponse from FastAPI
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseDetectionResponse {
    
    @JsonProperty("release_frame")
    private Integer releaseFrame;
    
    @JsonProperty("release_confirmed")
    private Boolean releaseConfirmed;
    
    @JsonProperty("total_frames")
    private Integer totalFrames;
    
    @JsonProperty("video_width")
    private Integer videoWidth;
    
    @JsonProperty("video_height")
    private Integer videoHeight;
    
    @JsonProperty("fps")
    private Integer fps;
    
    @JsonProperty("release_angle_deg")
    private Double releaseAngleDeg;
    
    @JsonProperty("release_lateral_offset_norm")
    private Double releaseLateralOffsetNorm;
    
    @JsonProperty("elbow_angle_deg")
    private Double elbowAngleDeg;
    
    @JsonProperty("shoulder_angle_deg")
    private Double shoulderAngleDeg;
    
    @JsonProperty("wrist_angle_deg")
    private Double wristAngleDeg;
    
    @JsonProperty("message")
    private String message;
}

