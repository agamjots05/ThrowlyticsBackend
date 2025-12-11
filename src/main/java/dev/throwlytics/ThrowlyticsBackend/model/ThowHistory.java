package dev.throwlytics.ThrowlyticsBackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "throwHistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThowHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long throwId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @JsonIgnore  // Don't serialize user field in API responses
    private User user;
    
    // Discus throw metrics (nullable - calculated later or from future analysis)
    @Column(nullable = true)
    private Integer releaseAngle;
    
    @Column(nullable = true)
    private Integer releaseHeight;
    
    // Video processing results (nullable - may not be available if processing fails)
    @Column(nullable = true)
    private Integer releaseFrame;
    
    @Column(nullable = true)
    private Boolean releaseConfirmed;
    
    @Column(nullable = true)
    private Integer totalFrames;
    
    @Column(nullable = true)
    private Integer videoWidth;
    
    @Column(nullable = true)
    private Integer videoHeight;
    
    @Column(nullable = true)
    private Integer fps;

    @Column(nullable = true)
    private Double releaseAngleDeg;

    @Column(nullable = true)
    private Double releaseLateralOffsetNorm;

    @Column(nullable = true)
    private Double elbowAngleDeg;

    @Column(nullable = true)
    private Double shoulderAngleDeg;

    @Column(nullable = true)
    private Double wristAngleDeg;
    
    // Video metadata
    private String thumbnailUrl;
    private String videoUrl;
    private LocalDateTime uploadDate;
}
