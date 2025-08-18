package com.demo.springboot.config;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.demo.springboot.entity.User;
import com.demo.springboot.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Custom Authentication Provider Tests")
class CustomAuthenticationProviderTest {

    private CustomAuthenticationProvider authProvider;
    
    @Mock
    private UserDetailsService userDetailsService;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authProvider = new CustomAuthenticationProvider();
        ReflectionTestUtils.setField(authProvider, "userDetailsService", userDetailsService);
        ReflectionTestUtils.setField(authProvider, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(authProvider, "userRepository", userRepository);
        ReflectionTestUtils.setField(authProvider, "maxAttempts", 5);
        ReflectionTestUtils.setField(authProvider, "lockDurationMinutes", 15);
    }

    @Nested
    @DisplayName("Successful Authentication Tests")
    class SuccessfulAuthenticationTests {

        @Test
        @DisplayName("Should authenticate valid credentials")
        void shouldAuthenticateValidCredentials() {
            // Arrange
            String username = "test@example.com";
            String password = "validPassword123";
            User user = createTestUser(username, "encodedPassword", true, 0, null);
            
            when(authentication.getName()).thenReturn(username);
            when(authentication.getCredentials()).thenReturn(password);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
            
            // Act
            Authentication result = authProvider.authenticate(authentication);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isAuthenticated());
            assertEquals(user, result.getPrincipal());
            verify(userRepository).save(user);
            assertEquals(0, user.getFailedLoginAttempts());
            assertTrue(user.isAccountNonLocked());
            assertNull(user.getLockTime());
        }

        @Test
        @DisplayName("Should reset failed attempts on successful login")
        void shouldResetFailedAttemptsOnSuccessfulLogin() {
            // Arrange
            String username = "test@example.com";
            String password = "validPassword123";
            User user = createTestUser(username, "encodedPassword", false, 3, LocalDateTime.now());
            
            when(authentication.getName()).thenReturn(username);
            when(authentication.getCredentials()).thenReturn(password);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
            
            // Act
            Authentication result = authProvider.authenticate(authentication);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isAuthenticated());
            assertEquals(0, user.getFailedLoginAttempts());
            assertTrue(user.isAccountNonLocked());
            assertNull(user.getLockTime());
            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("Account Lockout Tests")
    class AccountLockoutTests {

        @Test
        @DisplayName("Should lock account after max failed attempts")
        void shouldLockAccountAfterMaxFailedAttempts() {
            // Arrange
            String username = "test@example.com";
            String password = "wrongPassword";
            User user = createTestUser(username, "encodedPassword", true, 4, null);
            
            when(authentication.getName()).thenReturn(username);
            when(authentication.getCredentials()).thenReturn(password);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);
            
            // Act & Assert
            assertThrows(BadCredentialsException.class, () -> {
                authProvider.authenticate(authentication);
            });
            
            assertEquals(5, user.getFailedLoginAttempts());
            assertFalse(user.isAccountNonLocked());
            assertNotNull(user.getLockTime());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should prevent login when account is locked")
        void shouldPreventLoginWhenAccountIsLocked() {
            // Arrange
            String username = "test@example.com";
            String password = "validPassword123";
            LocalDateTime lockTime = LocalDateTime.now().minusMinutes(5); // Locked 5 minutes ago
            User user = createTestUser(username, "encodedPassword", false, 5, lockTime);
            
            when(authentication.getName()).thenReturn(username);
            when(authentication.getCredentials()).thenReturn(password);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
            
            // Act & Assert
            BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
                authProvider.authenticate(authentication);
            });
            
            assertEquals("Authentication failed: Account is locked. Try again later.", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should unlock account after lock duration expires")
        void shouldUnlockAccountAfterLockDurationExpires() {
            // Arrange
            String username = "test@example.com";
            String password = "validPassword123";
            LocalDateTime lockTime = LocalDateTime.now().minusMinutes(20); // Locked 20 minutes ago
            User user = createTestUser(username, "encodedPassword", false, 5, lockTime);
            
            when(authentication.getName()).thenReturn(username);
            when(authentication.getCredentials()).thenReturn(password);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
            
            // Act
            Authentication result = authProvider.authenticate(authentication);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isAuthenticated());
            assertEquals(0, user.getFailedLoginAttempts());
            assertTrue(user.isAccountNonLocked());
            assertNull(user.getLockTime());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should increment failed attempts on wrong password")
        void shouldIncrementFailedAttemptsOnWrongPassword() {
            // Arrange
            String username = "test@example.com";
            String password = "wrongPassword";
            User user = createTestUser(username, "encodedPassword", true, 2, null);
            
            when(authentication.getName()).thenReturn(username);
            when(authentication.getCredentials()).thenReturn(password);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);
            
            // Act & Assert
            assertThrows(BadCredentialsException.class, () -> {
                authProvider.authenticate(authentication);
            });
            
            assertEquals(3, user.getFailedLoginAttempts());
            assertTrue(user.isAccountNonLocked()); // Not locked yet
            assertNull(user.getLockTime());
            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("User Not Found Tests")
    class UserNotFoundTests {

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            String username = "nonexistent@example.com";
            String password = "anyPassword";
            
            when(authentication.getName()).thenReturn(username);
            when(authentication.getCredentials()).thenReturn(password);
            when(userRepository.findByEmail(username)).thenReturn(Optional.empty());
            
            // Act & Assert
            BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
                authProvider.authenticate(authentication);
            });
            
            assertEquals("Authentication failed: User not found", exception.getMessage());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Password Complexity Analysis Tests")
    class PasswordComplexityAnalysisTests {

        @Test
        @DisplayName("Should analyze password complexity correctly")
        void shouldAnalyzePasswordComplexityCorrectly() {
            // Test strong password
            String strongPassword = "StrongPass123!";
            String complexity = invokeAnalyzePasswordComplexity(strongPassword);
            assertTrue(complexity.contains("Strong"));
            assertTrue(complexity.contains("U:true"));
            assertTrue(complexity.contains("L:true"));
            assertTrue(complexity.contains("D:true"));
            assertTrue(complexity.contains("S:true"));
            
            // Test weak password
            String weakPassword = "weak";
            complexity = invokeAnalyzePasswordComplexity(weakPassword);
            assertTrue(complexity.contains("Weak"));
            
            // Test empty password
            complexity = invokeAnalyzePasswordComplexity("");
            assertEquals("Empty", complexity);
            
            // Test null password
            complexity = invokeAnalyzePasswordComplexity(null);
            assertEquals("Empty", complexity);
        }

        private String invokeAnalyzePasswordComplexity(String password) {
            return (String) ReflectionTestUtils.invokeMethod(authProvider, "analyzePasswordComplexity", password);
        }
    }

    @Nested
    @DisplayName("Hash Algorithm Detection Tests")
    class HashAlgorithmDetectionTests {

        @Test
        @DisplayName("Should detect hash algorithms correctly")
        void shouldDetectHashAlgorithmsCorrectly() {
            // Test BCrypt 2a
            String bcrypt2a = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";
            String algorithm = invokeDetectHashAlgorithm(bcrypt2a);
            assertEquals("BCrypt (2a)", algorithm);
            
            // Test BCrypt 2b
            String bcrypt2b = "$2b$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";
            algorithm = invokeDetectHashAlgorithm(bcrypt2b);
            assertEquals("BCrypt (2b)", algorithm);
            
            // Test empty hash
            algorithm = invokeDetectHashAlgorithm("");
            assertEquals("None", algorithm);
            
            // Test null hash
            algorithm = invokeDetectHashAlgorithm(null);
            assertEquals("None", algorithm);
            
            // Test unknown hash
            algorithm = invokeDetectHashAlgorithm("unknown_hash");
            assertEquals("Unknown/Plain", algorithm);
        }

        private String invokeDetectHashAlgorithm(String hash) {
            return (String) ReflectionTestUtils.invokeMethod(authProvider, "detectHashAlgorithm", hash);
        }
    }

    @Nested
    @DisplayName("Support Tests")
    class SupportTests {

        @Test
        @DisplayName("Should support UsernamePasswordAuthenticationToken")
        void shouldSupportUsernamePasswordAuthenticationToken() {
            assertTrue(authProvider.supports(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Should not support other authentication types")
        void shouldNotSupportOtherAuthenticationTypes() {
            assertFalse(authProvider.supports(String.class));
            assertFalse(authProvider.supports(Integer.class));
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should use configured max attempts")
        void shouldUseConfiguredMaxAttempts() {
            ReflectionTestUtils.setField(authProvider, "maxAttempts", 3);
            
            String username = "test@example.com";
            String password = "wrongPassword";
            User user = createTestUser(username, "encodedPassword", true, 2, null);
            
            when(authentication.getName()).thenReturn(username);
            when(authentication.getCredentials()).thenReturn(password);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);
            
            // First failed attempt
            assertThrows(BadCredentialsException.class, () -> {
                authProvider.authenticate(authentication);
            });
            
            // Second failed attempt
            assertThrows(BadCredentialsException.class, () -> {
                authProvider.authenticate(authentication);
            });
            
            // Third failed attempt should lock account
            assertThrows(BadCredentialsException.class, () -> {
                authProvider.authenticate(authentication);
            });
            
            assertFalse(user.isAccountNonLocked());
            assertEquals(3, user.getFailedLoginAttempts());
        }

        @Test
        @DisplayName("Should use configured lock duration")
        void shouldUseConfiguredLockDuration() {
            ReflectionTestUtils.setField(authProvider, "lockDurationMinutes", 30);
            
            String username = "test@example.com";
            String password = "validPassword123";
            LocalDateTime lockTime = LocalDateTime.now().minusMinutes(25); // Locked 25 minutes ago
            User user = createTestUser(username, "encodedPassword", false, 5, lockTime);
            
            when(authentication.getName()).thenReturn(username);
            when(authentication.getCredentials()).thenReturn(password);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
            
            // Should still be locked (25 < 30)
            BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
                authProvider.authenticate(authentication);
            });
            
            assertEquals("Account is locked. Try again later.", exception.getMessage());
        }
    }

    private User createTestUser(String email, String password, boolean accountNonLocked, 
                               int failedLoginAttempts, LocalDateTime lockTime) {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("USER");
        user.setAccountNonLocked(accountNonLocked);
        user.setFailedLoginAttempts(failedLoginAttempts);
        user.setLockTime(lockTime);
        return user;
    }
} 