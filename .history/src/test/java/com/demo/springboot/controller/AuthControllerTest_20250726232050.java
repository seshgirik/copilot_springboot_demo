package com.demo.springboot.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.demo.springboot.config.JwtUtil;
import com.demo.springboot.entity.User;
import com.demo.springboot.repository.UserRepository;
import com.demo.springboot.service.CustomUserDetailsService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

    private AuthController authController;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private CustomUserDetailsService userDetailsService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authController = new AuthController();
        ReflectionTestUtils.setField(authController, "authenticationManager", authenticationManager);
        ReflectionTestUtils.setField(authController, "userDetailsService", userDetailsService);
        ReflectionTestUtils.setField(authController, "userRepository", userRepository);
        ReflectionTestUtils.setField(authController, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(authController, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(authController, "minPasswordLength", 8);
    }

    @Nested
    @DisplayName("Password Strength Validation Tests")
    class PasswordStrengthValidationTests {

        @Test
        @DisplayName("Should accept strong passwords")
        void shouldAcceptStrongPasswords() {
            assertTrue(invokeIsPasswordStrong("StrongPass123!"));
            assertTrue(invokeIsPasswordStrong("MySecureP@ssw0rd"));
            assertTrue(invokeIsPasswordStrong("Complex#Password1"));
        }

        @Test
        @DisplayName("Should reject weak passwords")
        void shouldRejectWeakPasswords() {
            // Too short
            assertFalse(invokeIsPasswordStrong("weak"));
            assertFalse(invokeIsPasswordStrong("123"));
            
            // Missing uppercase
            assertFalse(invokeIsPasswordStrong("password123!"));
            
            // Missing lowercase
            assertFalse(invokeIsPasswordStrong("PASSWORD123!"));
            
            // Missing digit
            assertFalse(invokeIsPasswordStrong("Password!"));
            
            // Missing special character
            assertFalse(invokeIsPasswordStrong("Password123"));
        }

        @Test
        @DisplayName("Should handle null and empty passwords")
        void shouldHandleNullAndEmptyPasswords() {
            assertFalse(invokeIsPasswordStrong(null));
            assertFalse(invokeIsPasswordStrong(""));
            assertFalse(invokeIsPasswordStrong("   "));
        }

        @Test
        @DisplayName("Should respect minimum password length")
        void shouldRespectMinimumPasswordLength() {
            ReflectionTestUtils.setField(authController, "minPasswordLength", 12);
            
            // Valid password with 12+ characters
            assertTrue(invokeIsPasswordStrong("StrongPass123!"));
            
            // Invalid password with less than 12 characters
            assertFalse(invokeIsPasswordStrong("Pass123!"));
        }

        private boolean invokeIsPasswordStrong(String password) {
            return (Boolean) ReflectionTestUtils.invokeMethod(authController, "isPasswordStrong", password);
        }
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user with strong password")
        void shouldRegisterUserWithStrongPassword() {
            // Arrange
            User user = createTestUser("test@example.com", "StrongPass123!");
            
            when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(user);
            when(jwtUtil.generateToken(any(), eq("USER"))).thenReturn("jwt-token");
            
            // Act
            ResponseEntity<?> response = authController.registerUser(user);
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            assertEquals("jwt-token", responseBody.get("token"));
            assertEquals("USER", responseBody.get("role"));
            
            verify(userRepository).existsByEmail(user.getEmail());
            verify(passwordEncoder).encode(user.getPassword());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should reject user with weak password")
        void shouldRejectUserWithWeakPassword() {
            // Arrange
            User user = createTestUser("test@example.com", "weak");
            
            // Act
            ResponseEntity<?> response = authController.registerUser(user);
            
            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().toString().contains("Password must be at least"));
            
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject duplicate email")
        void shouldRejectDuplicateEmail() {
            // Arrange
            User user = createTestUser("existing@example.com", "StrongPass123!");
            
            when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);
            
            // Act
            ResponseEntity<?> response = authController.registerUser(user);
            
            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Email already registered", response.getBody());
            
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should set default role when not provided")
        void shouldSetDefaultRoleWhenNotProvided() {
            // Arrange
            User user = createTestUser("test@example.com", "StrongPass123!");
            user.setRole(null);
            
            when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(user);
            when(jwtUtil.generateToken(any(), eq("USER"))).thenReturn("jwt-token");
            
            // Act
            ResponseEntity<?> response = authController.registerUser(user);
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("USER", user.getRole());
        }

        @Test
        @DisplayName("Should preserve custom role when provided")
        void shouldPreserveCustomRoleWhenProvided() {
            // Arrange
            User user = createTestUser("test@example.com", "StrongPass123!");
            user.setRole("ADMIN");
            
            when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(user);
            when(jwtUtil.generateToken(any(), eq("ADMIN"))).thenReturn("jwt-token");
            
            // Act
            ResponseEntity<?> response = authController.registerUser(user);
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("ADMIN", user.getRole());
        }
    }

    @Nested
    @DisplayName("User Login Tests")
    class UserLoginTests {

        @Test
        @DisplayName("Should login with valid credentials")
        void shouldLoginWithValidCredentials() {
            // Arrange
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
            loginRequest.setEmail("test@example.com");
            loginRequest.setPassword("StrongPass123!");
            
            User user = createTestUser("test@example.com", "encodedPassword");
            
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(userDetailsService.loadUserByUsername(loginRequest.getEmail())).thenReturn(user);
            when(jwtUtil.generateToken(any(), eq("USER"))).thenReturn("jwt-token");
            
            // Act
            ResponseEntity<?> response = authController.authenticateUser(loginRequest);
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            assertEquals("jwt-token", responseBody.get("token"));
            assertEquals("USER", responseBody.get("role"));
        }

        @Test
        @DisplayName("Should reject invalid credentials")
        void shouldRejectInvalidCredentials() {
            // Arrange
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
            loginRequest.setEmail("test@example.com");
            loginRequest.setPassword("wrongPassword");
            
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
            
            // Act
            ResponseEntity<?> response = authController.authenticateUser(loginRequest);
            
            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Invalid email or password", response.getBody());
        }

        @Test
        @DisplayName("Should handle authentication exceptions")
        void shouldHandleAuthenticationExceptions() {
            // Arrange
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
            loginRequest.setEmail("test@example.com");
            loginRequest.setPassword("StrongPass123!");
            
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Database error"));
            
            // Act
            ResponseEntity<?> response = authController.authenticateUser(loginRequest);
            
            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Invalid email or password", response.getBody());
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate valid token")
        void shouldValidateValidToken() {
            // Arrange
            String token = "Bearer valid-jwt-token";
            User user = createTestUser("test@example.com", "encodedPassword");
            
            when(jwtUtil.extractUsername("valid-jwt-token")).thenReturn("test@example.com");
            when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(user);
            when(jwtUtil.validateToken("valid-jwt-token", user)).thenReturn(true);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            
            // Act
            ResponseEntity<?> response = authController.validateToken(token);
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            assertTrue((Boolean) responseBody.get("valid"));
            
            Map<String, Object> userInfo = (Map<String, Object>) responseBody.get("user");
            assertEquals(user.getId(), userInfo.get("id"));
            assertEquals(user.getName(), userInfo.get("name"));
            assertEquals(user.getEmail(), userInfo.get("email"));
            assertEquals(user.getRole(), userInfo.get("role"));
        }

        @Test
        @DisplayName("Should reject invalid token")
        void shouldRejectInvalidToken() {
            // Arrange
            String token = "Bearer invalid-jwt-token";
            
            when(jwtUtil.extractUsername("invalid-jwt-token")).thenReturn("test@example.com");
            when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(createTestUser("test@example.com", "encodedPassword"));
            when(jwtUtil.validateToken("invalid-jwt-token", any())).thenReturn(false);
            
            // Act
            ResponseEntity<?> response = authController.validateToken(token);
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            assertFalse((Boolean) responseBody.get("valid"));
        }

        @Test
        @DisplayName("Should handle token without Bearer prefix")
        void shouldHandleTokenWithoutBearerPrefix() {
            // Arrange
            String token = "valid-jwt-token";
            User user = createTestUser("test@example.com", "encodedPassword");
            
            when(jwtUtil.extractUsername("valid-jwt-token")).thenReturn("test@example.com");
            when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(user);
            when(jwtUtil.validateToken("valid-jwt-token", user)).thenReturn(true);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            
            // Act
            ResponseEntity<?> response = authController.validateToken(token);
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            assertTrue((Boolean) responseBody.get("valid"));
        }

        @Test
        @DisplayName("Should handle token validation exceptions")
        void shouldHandleTokenValidationExceptions() {
            // Arrange
            String token = "Bearer invalid-token";
            
            when(jwtUtil.extractUsername("invalid-token")).thenThrow(new RuntimeException("Token parsing error"));
            
            // Act
            ResponseEntity<?> response = authController.validateToken(token);
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            assertFalse((Boolean) responseBody.get("valid"));
        }
    }

    @Nested
    @DisplayName("Debug Endpoints Tests")
    class DebugEndpointsTests {

        @Test
        @DisplayName("Should return debug users information")
        void shouldReturnDebugUsersInformation() {
            // Arrange
            User user1 = createTestUser("user1@example.com", "encodedPassword1");
            User user2 = createTestUser("user2@example.com", "encodedPassword2");
            
            when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
            
            // Act
            ResponseEntity<Map<String, Object>> response = authController.debugUsers();
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            
            Map<String, Object> responseBody = response.getBody();
            assertEquals(2, responseBody.get("totalUsers"));
            
            List<Map<String, Object>> users = (List<Map<String, Object>>) responseBody.get("users");
            assertEquals(2, users.size());
            assertEquals("user1@example.com", users.get(0).get("email"));
            assertEquals("user2@example.com", users.get(1).get("email"));
        }
    }

    private User createTestUser(String email, String password) {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("USER");
        user.setAccountNonLocked(true);
        user.setFailedLoginAttempts(0);
        return user;
    }
} 