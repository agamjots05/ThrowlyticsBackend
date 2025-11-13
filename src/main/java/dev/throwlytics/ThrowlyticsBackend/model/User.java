package dev.throwlytics.ThrowlyticsBackend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;


    @Enumerated(EnumType.STRING)
    private PlanType planType;

    private int monthlyTokenLimit;
    private LocalDateTime lastTokenReset;

}
