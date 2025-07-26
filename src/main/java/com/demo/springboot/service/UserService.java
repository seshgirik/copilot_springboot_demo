package com.demo.springboot.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.demo.springboot.entity.User;
import com.demo.springboot.repository.UserRepository;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    public List<User> getAllUsers() {
        logger.info("👥 Service: Getting all users");
        List<User> users = userRepository.findAll();
        logger.info("✅ Service: Found {} users", users.size());
        if (logger.isDebugEnabled() && !users.isEmpty()) {
            users.forEach(u -> logger.debug("👤 User: ID={}, Name='{}', Email='{}'", 
                                           u.getId(), u.getName(), u.getEmail()));
        }
        return users;
    }
    
    public Page<User> getAllUsers(Pageable pageable) {
        logger.info("👥 Service: Getting users with pagination - page: {}, size: {}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        Page<User> users = userRepository.findAll(pageable);
        logger.info("✅ Service: Found {} users (page {} of {})", 
                   users.getNumberOfElements(), users.getNumber() + 1, users.getTotalPages());
        if (logger.isDebugEnabled() && users.hasContent()) {
            users.getContent().forEach(u -> logger.debug("👤 User: ID={}, Name='{}', Email='{}'", 
                                                        u.getId(), u.getName(), u.getEmail()));
        }
        return users;
    }
    
    public Optional<User> getUserById(Long id) {
        logger.info("🔍 Service: Getting user by ID: {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            logger.info("✅ Service: Found user: {} ({})", user.get().getName(), user.get().getEmail());
            logger.debug("👤 User details: ID={}, Name='{}', Email='{}', Phone='{}'", 
                        user.get().getId(), user.get().getName(), user.get().getEmail(), user.get().getPhone());
        } else {
            logger.warn("❌ Service: User not found with ID: {}", id);
        }
        return user;
    }
    
    public Optional<User> getUserByEmail(String email) {
        logger.info("🔍 Service: Getting user by email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            logger.info("✅ Service: Found user by email: {} (ID: {})", user.get().getName(), user.get().getId());
            logger.debug("👤 User details: ID={}, Name='{}', Phone='{}'", 
                        user.get().getId(), user.get().getName(), user.get().getPhone());
        } else {
            logger.warn("❌ Service: User not found with email: {}", email);
        }
        return user;
    }
    
    public List<User> searchUsersByName(String name) {
        logger.info("🔍 Service: Searching users by name containing: '{}'", name);
        logger.debug("🔍 Service: Executing case-insensitive name search query");
        List<User> users = userRepository.findByNameContainingIgnoreCase(name);
        logger.info("✅ Service: Found {} users matching name '{}'", users.size(), name);
        if (logger.isDebugEnabled() && !users.isEmpty()) {
            users.forEach(u -> logger.debug("👤 Found user: ID={}, Name='{}', Email='{}'", 
                                           u.getId(), u.getName(), u.getEmail()));
        }
        return users;
    }
    
    public List<User> searchUsersByPhone(String phone) {
        logger.info("🔍 Service: Searching users by phone containing: '{}'", phone);
        logger.debug("🔍 Service: Executing phone search query");
        List<User> users = userRepository.findByPhoneContaining(phone);
        logger.info("✅ Service: Found {} users matching phone '{}'", users.size(), phone);
        if (logger.isDebugEnabled() && !users.isEmpty()) {
            users.forEach(u -> logger.debug("📞 Found user: ID={}, Name='{}', Phone='{}'", 
                                           u.getId(), u.getName(), u.getPhone()));
        }
        return users;
    }
    
    public User createUser(User user) {
        logger.info("🆕 Service: Creating new user: {} ({})", user.getName(), user.getEmail());
        logger.debug("🔍 Service: Checking if user with email '{}' already exists", user.getEmail());
        
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.error("❌ Service: User creation failed - email '{}' already exists", user.getEmail());
            throw new RuntimeException("User with email " + user.getEmail() + " already exists");
        }
        
        logger.debug("📝 Service: User details - Name: '{}', Email: '{}', Phone: '{}'", 
                    user.getName(), user.getEmail(), user.getPhone());
        logger.debug("💾 Service: Saving new user to database");
        
        User savedUser = userRepository.save(user);
        logger.info("✅ Service: User created successfully with ID: {}", savedUser.getId());
        
        return savedUser;
    }
    
    public User updateUser(Long id, User userDetails) {
        logger.info("🔄 Service: Updating user with ID: {}", id);
        logger.debug("🔍 Service: Searching for user to update");
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("❌ Service: User not found for update with ID: {}", id);
                    return new RuntimeException("User not found with id: " + id);
                });
        
        logger.debug("📋 Service: Current user details - Name: '{}', Email: '{}', Phone: '{}'", 
                    user.getName(), user.getEmail(), user.getPhone());
        logger.debug("📝 Service: New user details - Name: '{}', Email: '{}', Phone: '{}'", 
                    userDetails.getName(), userDetails.getEmail(), userDetails.getPhone());
        
        // Check if email is being changed and if new email already exists
        if (!user.getEmail().equals(userDetails.getEmail())) {
            logger.debug("📧 Service: Email change detected - checking if new email '{}' exists", userDetails.getEmail());
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                logger.error("❌ Service: User update failed - email '{}' already exists", userDetails.getEmail());
                throw new RuntimeException("User with email " + userDetails.getEmail() + " already exists");
            }
        }
        
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        
        logger.debug("💾 Service: Saving updated user to database");
        User updatedUser = userRepository.save(user);
        logger.info("✅ Service: User updated successfully - ID: {}, Name: '{}', Email: '{}'", 
                   updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail());
        
        return updatedUser;
    }
    
    public void deleteUser(Long id) {
        logger.info("🗑️ Service: Deleting user with ID: {}", id);
        logger.debug("🔍 Service: Searching for user to delete");
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("❌ Service: User not found for deletion with ID: {}", id);
                    return new RuntimeException("User not found with id: " + id);
                });
        
        logger.debug("📋 Service: Found user to delete - Name: '{}', Email: '{}'", 
                    user.getName(), user.getEmail());
        logger.debug("💾 Service: Executing delete operation");
        
        userRepository.delete(user);
        logger.info("✅ Service: User deleted successfully - ID: {}, Name: '{}', Email: '{}'", 
                   id, user.getName(), user.getEmail());
    }
    
    public boolean existsByEmail(String email) {
        logger.debug("🔍 Service: Checking if user exists with email: {}", email);
        boolean exists = userRepository.existsByEmail(email);
        logger.debug("✅ Service: User exists check for '{}': {}", email, exists);
        return exists;
    }
    
    public long getTotalCount() {
        logger.info("🔍 Service: Getting total user count");
        long count = userRepository.count();
        logger.info("✅ Service: Total users count: {}", count);
        return count;
    }
}
