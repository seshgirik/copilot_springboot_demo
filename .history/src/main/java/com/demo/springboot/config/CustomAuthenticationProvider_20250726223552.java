package com.demo.springboot.config;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.demo.springboot.entity.User;
import com.demo.springboot.repository.UserRepository;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        logger.info("        🔐 CustomAuthenticationProvider - Starting detailed password validation");
        logger.info("            📋 Authentication Request Details:");
        logger.info("                - Username/Email: {}", username);
        logger.info("                - Password length: {} characters", password.length());
        logger.info("                - Password complexity: {}", analyzePasswordComplexity(password));

        try {
            logger.info("            🔍 Step 1: Loading user details");
            User user = userRepository.findByEmail(username).orElseThrow(() -> new BadCredentialsException("User not found"));
            if (!user.isAccountNonLocked()) {
                if (user.getLockTime() != null && user.getLockTime().plusMinutes(15).isBefore(LocalDateTime.now())) {
                    // Unlock account after lock duration
                    user.setAccountNonLocked(true);
                    user.setFailedLoginAttempts(0);
                    user.setLockTime(null);
                    userRepository.save(user);
                } else {
                    logger.error("        ⛔ Account is locked due to too many failed login attempts");
                    throw new BadCredentialsException("Account is locked. Try again later.");
                }
            }
            logger.info("            ✅ Step 1: User details loaded successfully");

            logger.info("            🔐 Step 2: Password verification process");
            logger.info("                📊 Password Comparison Details:");
            logger.info("                    - Input password: [PROTECTED]");
            logger.info("                    - Input password length: {} chars", password.length());
            logger.info("                    - Stored hash: [PROTECTED]");
            logger.info("                    - Stored hash length: {} chars", user.getPassword().length());
            logger.info("                    - Hash algorithm: {}", detectHashAlgorithm(user.getPassword()));

            logger.info("                🧮 Executing BCrypt password matching...");
            long startTime = System.currentTimeMillis();
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            long endTime = System.currentTimeMillis();
            
            logger.info("                ⏱️  BCrypt verification completed in {} ms", (endTime - startTime));
            logger.info("                🎯 Password match result: {}", passwordMatches ? "✅ SUCCESS" : "❌ FAILURE");

            if (passwordMatches) {
                logger.info("            ✅ Step 2: Password verification successful");
                logger.info("            🏗️  Step 3: Creating authenticated token");
                // Reset failed attempts on successful login
                if (user.getFailedLoginAttempts() > 0 || !user.isAccountNonLocked()) {
                    user.setFailedLoginAttempts(0);
                    user.setAccountNonLocked(true);
                    user.setLockTime(null);
                    userRepository.save(user);
                }
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
                
                logger.info("            ✅ Step 3: Authentication token created successfully");
                logger.info("                📊 Token Details:");
                logger.info("                    - Principal: {}", authToken.getPrincipal().getClass().getSimpleName());
                logger.info("                    - Authorities: {}", authToken.getAuthorities());
                logger.info("                    - Authenticated: {}", authToken.isAuthenticated());
                
                logger.info("        ✅ CustomAuthenticationProvider - Authentication completed successfully");
                return authToken;
            } else {
                // Increment failed attempts
                int attempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(attempts);
                if (attempts >= 5) {
                    user.setAccountNonLocked(false);
                    user.setLockTime(LocalDateTime.now());
                    logger.error("        ⛔ Account locked due to 5 failed login attempts");
                }
                userRepository.save(user);
                logger.error("            ❌ Step 2: Password verification failed");
                logger.error("                🔍 Failure Analysis:");
                logger.error("                    - Reason: Password does not match stored hash");
                logger.error("                    - Input password: [PROTECTED]");
                logger.error("                    - Expected pattern: BCrypt hash starting with $2a/$2b/$2y");
                logger.error("                    - Actual hash prefix: [PROTECTED]");
                
                logger.error("        ❌ CustomAuthenticationProvider - Authentication failed");
                throw new BadCredentialsException("Invalid password");
            }
        } catch (Exception e) {
            logger.error("            ❌ Authentication error occurred: {}", e.getMessage());
            logger.error("            🔍 Error Details:");
            logger.error("                - Exception type: {}", e.getClass().getSimpleName());
            logger.error("                - Error message: {}", e.getMessage());
            logger.error("                - Username attempted: {}", username);
            
            logger.error("        ❌ CustomAuthenticationProvider - Authentication failed with exception");
            throw new BadCredentialsException("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private String analyzePasswordComplexity(String password) {
        if (password == null || password.isEmpty()) {
            return "Empty";
        }
        
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
        
        int complexity = 0;
        if (hasUpper) complexity++;
        if (hasLower) complexity++;
        if (hasDigit) complexity++;
        if (hasSpecial) complexity++;
        
        String level = switch (complexity) {
            case 0, 1 -> "Weak";
            case 2 -> "Fair";
            case 3 -> "Good";
            case 4 -> "Strong";
            default -> "Unknown";
        };
        
        return String.format("%s (U:%s L:%s D:%s S:%s)", level, hasUpper, hasLower, hasDigit, hasSpecial);
    }

    private String detectHashAlgorithm(String hash) {
        if (hash == null || hash.isEmpty()) {
            return "None";
        }
        
        if (hash.startsWith("$2a$")) return "BCrypt (2a)";
        if (hash.startsWith("$2b$")) return "BCrypt (2b)";
        if (hash.startsWith("$2y$")) return "BCrypt (2y)";
        if (hash.startsWith("{bcrypt}")) return "BCrypt (Spring format)";
        if (hash.startsWith("{SHA-256}")) return "SHA-256";
        if (hash.startsWith("{MD5}")) return "MD5";
        
        return "Unknown/Plain";
    }
}
