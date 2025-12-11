package dev.throwlytics.ThrowlyticsBackend.controller;

import dev.throwlytics.ThrowlyticsBackend.dto.LoginRequest;
import dev.throwlytics.ThrowlyticsBackend.dto.LoginResponse;
import dev.throwlytics.ThrowlyticsBackend.dto.SignupRequest;
import dev.throwlytics.ThrowlyticsBackend.dto.SignupResponse;
import dev.throwlytics.ThrowlyticsBackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * User signup endpoint
     * POST /api/auth/signup
     * 
     * Request body:
     * {
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "password": "password123"
     * }
     * 
     * Success response (201 Created):
     * {
     *   "userId": 1,
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "planType": "FREE",
     *   "monthlyTokenLimit": 5,
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     * 
     * Error response (400 Bad Request):
     * {
     *   "message": "Email already exists",
     *   "timestamp": "2025-01-07T10:30:00",
     *   "details": ["user@example.com is already registered"]
     * }
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * User login endpoint
     * POST /api/auth/login
     * 
     * Request body:
     * {
     *   "email": "john@example.com",
     *   "password": "password123"
     * }
     * 
     * Success response (200 OK):
     * {
     *   "userId": 1,
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "planType": "FREE",
     *   "monthlyTokenLimit": 5,
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     * 
     * Error response (400 Bad Request):
     * {
     *   "message": "Invalid email or password",
     *   "timestamp": "2025-01-07T10:30:00",
     *   "details": null
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check endpoint
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}

