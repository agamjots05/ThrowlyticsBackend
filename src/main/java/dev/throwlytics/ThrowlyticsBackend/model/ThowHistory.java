package dev.throwlytics.ThrowlyticsBackend.model;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="throw_history")
public class ThowHistory {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long throw_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User id;

    //Discus throw metrics
    private int release_angle;
    private int release_height;


    //Video metadata
    private String thumbnail_url;
    private String video_url;
    private LocalDateTime upload_date;


}
