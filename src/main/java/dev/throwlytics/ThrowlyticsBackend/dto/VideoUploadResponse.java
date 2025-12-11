package dev.throwlytics.ThrowlyticsBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for video upload and processing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoUploadResponse {
    private Long throwId;
    private Long userId;
    private Integer releaseFrame;
    private Boolean releaseConfirmed;
    private Integer totalFrames;
    private Integer videoWidth;
    private Integer videoHeight;
    private Integer fps;

    // Additional metrics
    private Double releaseAngleDeg;
    private Double releaseLateralOffsetNorm;
    private Double elbowAngleDeg;
    private Double shoulderAngleDeg;
    private Double wristAngleDeg;

    private String videoUrl;
    private String thumbnailUrl;
    private LocalDateTime uploadDate;
    private String message;
}

