package com.demo.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.demo.springboot.entity.User;
import com.demo.springboot.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("    🔍 Loading user details from database for email: {}", email);
        logger.info("    📋 UserDetailsService Password Validation Flow:");
        logger.info("        - Searching user by email in database");
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("    ❌ User not found with email: {}", email);
                    logger.error("    🔍 Database Search Results:");
                    logger.error("        - Email searched: {}", email);
                    logger.error("        - Result: No matching user found");
                    logger.error("        - Available users count: {}", userRepository.count());
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
                
        logger.info("    ✅ User found in database:");
        logger.info("        - ID: {}", user.getId());
        logger.info("        - Name: {}", user.getName());
        logger.info("        - Email: {}", user.getEmail());
        logger.info("        - Role: {}", user.getRole());
        logger.info("        - Enabled: {}", user.isEnabled());
        logger.info("        - Account Non-Expired: {}", user.isAccountNonExpired());
        logger.info("        - Account Non-Locked: {}", user.isAccountNonLocked());
        logger.info("        - Credentials Non-Expired: {}", user.isCredentialsNonExpired());
        logger.info("        - Authorities: {}", user.getAuthorities());
        
        logger.info("    🔐 Password Information:");
        logger.info("        - Password hash exists: {}", user.getPassword() != null && !user.getPassword().isEmpty());
        logger.info("        - Password hash length: {} characters", user.getPassword() != null ? user.getPassword().length() : 0);
        if (user.getPassword() != null && user.getPassword().length() > 10) {
            logger.info("        - Password hash prefix: {}...", user.getPassword().substring(0, 10));
            logger.info("        - Hash algorithm detected: {}", user.getPassword().startsWith("$2") ? "BCrypt" : "Unknown");
        }
        logger.info("        - UserDetails object ready for password comparison");
        
        return user;
    }
} 