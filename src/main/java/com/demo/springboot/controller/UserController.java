package com.demo.springboot.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.springboot.entity.User;
import com.demo.springboot.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users with optional pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved users")
    public ResponseEntity<Page<User>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        logger.info("🌐 REST: GET /api/users - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.getAllUsers(pageable);
        logger.info("🌐 REST: Returning {} users (page {} of {})", 
                   users.getNumberOfElements(), users.getNumber() + 1, users.getTotalPages());
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        
        logger.info("🌐 REST: GET /api/users/{}", id);
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            logger.info("🌐 REST: Returning user: {} ({})", user.get().getName(), user.get().getEmail());
            return ResponseEntity.ok(user.get());
        } else {
            logger.warn("🌐 REST: User not found, returning 404");
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieve a user by their email address")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<User> getUserByEmail(
            @Parameter(description = "User email") @PathVariable String email) {
        
        Optional<User> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by name or phone")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    public ResponseEntity<List<User>> searchUsers(
            @Parameter(description = "Search by name") @RequestParam(required = false) String name,
            @Parameter(description = "Search by phone") @RequestParam(required = false) String phone) {
        
        List<User> users;
        if (name != null && !name.isEmpty()) {
            users = userService.searchUsersByName(name);
        } else if (phone != null && !phone.isEmpty()) {
            users = userService.searchUsersByPhone(phone);
        } else {
            users = userService.getAllUsers();
        }
        
        return ResponseEntity.ok(users);
    }
    
    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "409", description = "User with email already exists")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<?> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody User userDetails) {
        
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user by ID")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/count")
    @Operation(summary = "Get user count", description = "Get total number of users")
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    public ResponseEntity<Long> getUserCount() {
        long count = userService.getTotalCount();
        return ResponseEntity.ok(count);
    }
}
