package dev.throwlytics.ThrowlyticsBackend.service;

import dev.throwlytics.ThrowlyticsBackend.dto.LoginRequest;
import dev.throwlytics.ThrowlyticsBackend.dto.LoginResponse;
import dev.throwlytics.ThrowlyticsBackend.dto.SignupRequest;
import dev.throwlytics.ThrowlyticsBackend.dto.SignupResponse;
import dev.throwlytics.ThrowlyticsBackend.model.User;
import dev.throwlytics.ThrowlyticsBackend.repository.UserRepository;
import dev.throwlytics.ThrowlyticsBackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Register a new user
     * @param request Signup request with name, email, password
     * @return SignupResponse with user info and JWT token
     * @throws RuntimeException if email already exists
     */
    public SignupResponse signup(SignupRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        
        // Hash password (NEVER store plain text!)
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Defaults are set by @PrePersist in User entity:
        // - planType = FREE
        // - monthlyTokenLimit = 5
        // - lastTokenReset = now()
        // - createdAt = now()
        // - updatedAt = now()
        
        // Save user to database
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser);
        
        // Return response
        return new SignupResponse(
            savedUser.getUserId(),
            savedUser.getName(),
            savedUser.getEmail(),
            savedUser.getPlanType(),
            savedUser.getMonthlyTokenLimit(),
            token
        );
    }
    
    /**
     * Login an existing user
     * @param request Login request with email and password
     * @return LoginResponse with user info and JWT token
     * @throws RuntimeException if email not found or password incorrect
     */
    public LoginResponse login(LoginRequest request) {
        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }
        
        User user = userOptional.get();
        
        // Check if password matches
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user);
        
        // Return response
        return new LoginResponse(
            user.getUserId(),
            user.getName(),
            user.getEmail(),
            user.getPlanType(),
            user.getMonthlyTokenLimit(),
            token
        );
    }
}

