package com.demo.springboot.controller;

import com.demo.springboot.config.JwtUtil;
import com.demo.springboot.entity.User;
import com.demo.springboot.repository.UserRepository;
import com.demo.springboot.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default role if not provided
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        User savedUser = userRepository.save(user);
        
        // Generate token for the new user
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtUtil.generateToken(userDetails, savedUser.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("token", token);
        response.put("user", Map.of(
            "id", savedUser.getId(),
            "name", savedUser.getName(),
            "email", savedUser.getEmail(),
            "role", savedUser.getRole()
        ));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
            
            String token = jwtUtil.generateToken(userDetails, user != null ? user.getRole() : "USER");

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("user", Map.of(
                "id", user != null ? user.getId() : null,
                "name", user != null ? user.getName() : null,
                "email", userDetails.getUsername(),
                "role", user != null ? user.getRole() : "USER"
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            String email = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            
            if (jwtUtil.validateToken(token, userDetails)) {
                User user = userRepository.findByEmail(email).orElse(null);
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "user", Map.of(
                        "id", user != null ? user.getId() : null,
                        "name", user != null ? user.getName() : null,
                        "email", email,
                        "role", user != null ? user.getRole() : "USER"
                    )
                ));
            } else {
                return ResponseEntity.ok(Map.of("valid", false));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    // Inner class for login request
    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
} 