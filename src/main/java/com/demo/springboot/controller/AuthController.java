package com.demo.springboot.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.springboot.config.JwtUtil;
import com.demo.springboot.entity.User;
import com.demo.springboot.repository.UserRepository;
import com.demo.springboot.service.CustomUserDetailsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

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
        logger.info("=== POST /auth/register - User Registration with Token Generation ===");
        logger.info("Step 1: Received registration request for email: {}", user.getEmail());
        
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("Step 1: ❌ Email already registered: {}", user.getEmail());
            return ResponseEntity.badRequest().body("Email already registered");
        }

        // Encode password
        logger.info("Step 2: Encoding user password");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info("Step 2: ✅ Password encoded successfully");
        
        // Set default role if not provided
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        logger.info("Step 3: User role set to: {}", user.getRole());

        logger.info("Step 4: Saving user to database");
        User savedUser = userRepository.save(user);
        logger.info("Step 4: ✅ User saved successfully with ID: {}", savedUser.getId());
        
        // Generate token for the new user
        logger.info("Step 5: Generating JWT token for newly registered user");
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtUtil.generateToken(userDetails, savedUser.getRole());
        logger.info("Step 5: ✅ JWT token generated for registration");

        logger.info("Step 6: Preparing registration response");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("token", token);
        response.put("user", Map.of(
            "id", savedUser.getId(),
            "name", savedUser.getName(),
            "email", savedUser.getEmail(),
            "role", savedUser.getRole()
        ));

        logger.info("Step 6: ✅ Registration response prepared successfully");
        logger.info("=== POST /auth/register - Registration and Token Generation Completed ===");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        logger.info("=== POST /auth/login - Token Generation Flow Started ===");
        logger.info("Step 1: Received login request for email: {}", loginRequest.getEmail());
        
        try {
            // Step 2: Detailed password validation process
            logger.info("Step 2: Starting detailed password validation process");
            logger.info("    🔐 Password Validation Flow:");
            logger.info("        - Input email: {}", loginRequest.getEmail());
            logger.info("        - Input password length: {} characters", loginRequest.getPassword().length());
            logger.info("        - Password starts with: {}...", loginRequest.getPassword().substring(0, Math.min(3, loginRequest.getPassword().length())));
            
            // Pre-authentication: Load user to show stored password info
            logger.info("    📋 Pre-Authentication: Loading user from database");
            User userFromDb = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
            if (userFromDb != null) {
                logger.info("        - User found in database: {}", userFromDb.getEmail());
                logger.info("        - Stored password hash length: {} characters", userFromDb.getPassword().length());
                logger.info("        - Stored password starts with: {}...", userFromDb.getPassword().substring(0, Math.min(10, userFromDb.getPassword().length())));
                logger.info("        - Password encoding algorithm: BCrypt (detected from hash prefix)");
                
                // Manual password verification for detailed logging
                logger.info("    🔍 Manual Password Verification:");
                boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), userFromDb.getPassword());
                logger.info("        - Raw password: {}", loginRequest.getPassword());
                logger.info("        - Encoded password (from DB): {}", userFromDb.getPassword());
                logger.info("        - Password verification result: {}", passwordMatches ? "✅ MATCH" : "❌ NO MATCH");
                
                if (!passwordMatches) {
                    logger.error("        ❌ Password validation failed - passwords do not match");
                    logger.error("        📊 Validation Details:");
                    logger.error("            - Expected password length: {} chars", userFromDb.getPassword().length());
                    logger.error("            - Provided password length: {} chars", loginRequest.getPassword().length());
                    logger.error("            - Hash algorithm: BCrypt");
                    throw new RuntimeException("Invalid password");
                } else {
                    logger.info("        ✅ Password validation successful - proceeding with authentication");
                }
            } else {
                logger.error("        ❌ User not found in database: {}", loginRequest.getEmail());
                throw new RuntimeException("User not found");
            }
            
            logger.info("Step 2: Authenticating user credentials with AuthenticationManager");
            logger.info("    🏗️  Creating UsernamePasswordAuthenticationToken");
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword());
            logger.info("        - Principal (username): {}", authToken.getPrincipal());
            logger.info("        - Credentials (password): [HIDDEN for security]");
            logger.info("        - Authentication token created successfully");
            
            logger.info("    🔐 Delegating to Spring Security AuthenticationManager");
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            logger.info("Step 2: ✅ Authentication successful for user: {}", loginRequest.getEmail());
            logger.info("    📊 Authentication Result Details:");
            logger.info("        - Authenticated: {}", authentication.isAuthenticated());
            logger.info("        - Principal: {}", authentication.getPrincipal().getClass().getSimpleName());
            logger.info("        - Authorities: {}", authentication.getAuthorities());

            // Step 3: Load user details
            logger.info("Step 3: Loading user details from database");
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            logger.info("Step 3: ✅ UserDetails loaded - Username: {}, Authorities: {}", 
                userDetails.getUsername(), userDetails.getAuthorities());
            
            // Step 4: Fetch user entity for role information
            logger.info("Step 4: Fetching User entity from database for role information");
            User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
            if (user != null) {
                logger.info("Step 4: ✅ User entity found - ID: {}, Name: {}, Email: {}, Role: {}", 
                    user.getId(), user.getName(), user.getEmail(), user.getRole());
            } else {
                logger.warn("Step 4: ⚠️  User entity not found, using default role");
            }
            
            // Step 5: Generate JWT token
            String userRole = user != null ? user.getRole() : "USER";
            logger.info("Step 5: Generating JWT token with role: {}", userRole);
            String token = jwtUtil.generateToken(userDetails, userRole);
            logger.info("Step 5: ✅ JWT token generated successfully");
            logger.debug("Step 5: Generated token: {}", token);

            // Step 6: Prepare response
            logger.info("Step 6: Preparing login response");
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("user", Map.of(
                "id", user != null ? user.getId() : null,
                "name", user != null ? user.getName() : null,
                "email", userDetails.getUsername(),
                "role", userRole
            ));
            
            logger.info("Step 6: ✅ Response prepared successfully");
            logger.info("=== POST /auth/login - Token Generation Flow Completed Successfully ===");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Authentication failed for user: {} - Error: {}", loginRequest.getEmail(), e.getMessage());
            logger.error("=== POST /auth/login - Token Generation Flow Failed ===");
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

    @GetMapping("/debug/users")
    public ResponseEntity<Map<String, Object>> debugUsers() {
        List<User> users = userRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", users.size());
        response.put("users", users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("name", user.getName());
            userMap.put("email", user.getEmail());
            userMap.put("phone", user.getPhone());
            userMap.put("role", user.getRole());
            userMap.put("password", user.getPassword()); // Include password hash for debugging
            return userMap;
        }).collect(Collectors.toList()));
        return ResponseEntity.ok(response);
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