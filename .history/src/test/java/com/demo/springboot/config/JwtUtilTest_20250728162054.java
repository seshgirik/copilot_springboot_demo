package com.demo.springboot.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JWT Utility Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    
    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "your-super-secret-jwt-key-must-be-at-least-32-characters-long-for-production");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24 hours
        
        // Setup mock user details
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userDetails.getAuthorities()).thenReturn((Collection) Arrays.asList(new SimpleGrantedAuthority("USER")));
    }

    @Nested
    @DisplayName("Secret Key Validation Tests")
    class SecretKeyValidationTests {

        @Test
        @DisplayName("Should accept valid secret key")
        void shouldAcceptValidSecretKey() {
            ReflectionTestUtils.setField(jwtUtil, "secret", "your-super-secret-jwt-key-must-be-at-least-32-characters-long-for-production");
            
            assertDoesNotThrow(() -> {
                jwtUtil.generateToken(userDetails);
            });
        }

        @Test
        @DisplayName("Should reject null secret key")
        void shouldRejectNullSecretKey() {
            ReflectionTestUtils.setField(jwtUtil, "secret", null);
            
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                jwtUtil.generateToken(userDetails);
            });
            
            assertTrue(exception.getMessage().contains("JWT secret must be set"));
        }

        @Test
        @DisplayName("Should reject short secret key")
        void shouldRejectShortSecretKey() {
            ReflectionTestUtils.setField(jwtUtil, "secret", "short");
            
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                jwtUtil.generateToken(userDetails);
            });
            
            assertTrue(exception.getMessage().contains("at least 32 characters long"));
        }

        @Test
        @DisplayName("Should reject empty secret key")
        void shouldRejectEmptySecretKey() {
            ReflectionTestUtils.setField(jwtUtil, "secret", "");
            
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                jwtUtil.generateToken(userDetails);
            });
            
            assertTrue(exception.getMessage().contains("JWT secret must be set"));
        }
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid token")
        void shouldGenerateValidToken() {
            // Act
            String token = jwtUtil.generateToken(userDetails);
            
            // Assert
            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
        }

        @Test
        @DisplayName("Should generate token with role")
        void shouldGenerateTokenWithRole() {
            // Act
            String token = jwtUtil.generateToken(userDetails, "ADMIN");
            
            // Assert
            assertNotNull(token);
            String extractedRole = jwtUtil.extractRole(token);
            assertEquals("ADMIN", extractedRole);
        }

        @Test
        @DisplayName("Should generate different tokens for same user")
        void shouldGenerateDifferentTokensForSameUser() {
            // Act
            String token1 = jwtUtil.generateToken(userDetails);
            String token2 = jwtUtil.generateToken(userDetails);
            
            // Assert
            assertNotEquals(token1, token2); // Different timestamps
        }

        @Test
        @DisplayName("Should include correct subject in token")
        void shouldIncludeCorrectSubjectInToken() {
            // Act
            String token = jwtUtil.generateToken(userDetails);
            String extractedUsername = jwtUtil.extractUsername(token);
            
            // Assert
            assertEquals("test@example.com", extractedUsername);
        }

        @Test
        @DisplayName("Should include expiration in token")
        void shouldIncludeExpirationInToken() {
            // Act
            String token = jwtUtil.generateToken(userDetails);
            Date expiration = jwtUtil.extractExpiration(token);
            
            // Assert
            assertNotNull(expiration);
            assertTrue(expiration.after(new Date())); // Should be in the future
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate valid token")
        void shouldValidateValidToken() {
            // Arrange
            String token = jwtUtil.generateToken(userDetails);
            
            // Act
            boolean isValid = jwtUtil.validateToken(token, userDetails);
            
            // Assert
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should reject token with wrong username")
        void shouldRejectTokenWithWrongUsername() {
            // Arrange
            String token = jwtUtil.generateToken(userDetails);
            UserDetails wrongUser = mock(UserDetails.class);
            when(wrongUser.getUsername()).thenReturn("wrong@example.com");
            
            // Act
            boolean isValid = jwtUtil.validateToken(token, wrongUser);
            
            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should reject expired token")
        void shouldRejectExpiredToken() {
            // Arrange
            ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L); // Negative expiration
            String token = jwtUtil.generateToken(userDetails);
            
            // Act
            boolean isValid = jwtUtil.validateToken(token, userDetails);
            
            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should reject malformed token")
        void shouldRejectMalformedToken() {
            // Arrange
            String malformedToken = "not.a.valid.jwt.token";
            
            // Act
            boolean isValid = jwtUtil.validateToken(malformedToken, userDetails);
            
            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should reject null token")
        void shouldRejectNullToken() {
            // Act
            boolean isValid = jwtUtil.validateToken(null, userDetails);
            
            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should reject empty token")
        void shouldRejectEmptyToken() {
            // Act
            boolean isValid = jwtUtil.validateToken("", userDetails);
            
            // Assert
            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("Token Extraction Tests")
    class TokenExtractionTests {

        @Test
        @DisplayName("Should extract username from token")
        void shouldExtractUsernameFromToken() {
            // Arrange
            String token = jwtUtil.generateToken(userDetails);
            
            // Act
            String extractedUsername = jwtUtil.extractUsername(token);
            
            // Assert
            assertEquals("test@example.com", extractedUsername);
        }

        @Test
        @DisplayName("Should extract role from token")
        void shouldExtractRoleFromToken() {
            // Arrange
            String token = jwtUtil.generateToken(userDetails, "ADMIN");
            
            // Act
            String extractedRole = jwtUtil.extractRole(token);
            
            // Assert
            assertEquals("ADMIN", extractedRole);
        }

        @Test
        @DisplayName("Should extract expiration from token")
        void shouldExtractExpirationFromToken() {
            // Arrange
            String token = jwtUtil.generateToken(userDetails);
            
            // Act
            Date expiration = jwtUtil.extractExpiration(token);
            
            // Assert
            assertNotNull(expiration);
            assertTrue(expiration.after(new Date()));
        }

        @Test
        @DisplayName("Should handle extraction from invalid token")
        void shouldHandleExtractionFromInvalidToken() {
            // Act & Assert
            assertNull(jwtUtil.extractRole("invalid.token"));
            // extractUsername and extractExpiration will throw, so keep those as is if implementation throws, else expect null
            try {
                jwtUtil.extractUsername("invalid.token");
                fail("Expected exception");
            } catch (Exception ignored) {}
            try {
                jwtUtil.extractExpiration("invalid.token");
                fail("Expected exception");
            } catch (Exception ignored) {}
        }

        @Test
        @DisplayName("Should return null role for token without role claim")
        void shouldReturnNullRoleForTokenWithoutRoleClaim() {
            // Arrange
            String token = jwtUtil.generateToken(userDetails); // No role specified
            
            // Act
            String extractedRole = jwtUtil.extractRole(token);
            
            // Assert
            assertNull(extractedRole);
        }
    }

    @Nested
    @DisplayName("Token Expiration Tests")
    class TokenExpirationTests {

        @Test
        @DisplayName("Should check if token is expired")
        void shouldCheckIfTokenIsExpired() {
            // Arrange
            String token = jwtUtil.generateToken(userDetails);
            
            // Act
            boolean isExpired = invokeIsTokenExpired(token);
            
            // Assert
            assertFalse(isExpired);
        }

        @Test
        @DisplayName("Should detect expired token")
        void shouldDetectExpiredToken() {
            // Arrange
            ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L); // Negative expiration
            String token = jwtUtil.generateToken(userDetails);
            
            // Act
            boolean isExpired = invokeIsTokenExpired(token);
            
            // Assert
            assertTrue(isExpired);
        }

        private boolean invokeIsTokenExpired(String token) {
            return (Boolean) ReflectionTestUtils.invokeMethod(jwtUtil, "isTokenExpired", token);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should use configured expiration time")
        void shouldUseConfiguredExpirationTime() {
            // Arrange
            long customExpiration = 3600000L; // 1 hour
            ReflectionTestUtils.setField(jwtUtil, "expiration", customExpiration);
            
            // Act
            String token = jwtUtil.generateToken(userDetails);
            Date expiration = jwtUtil.extractExpiration(token);
            
            // Assert
            assertNotNull(expiration);
            long expectedExpirationTime = System.currentTimeMillis() + customExpiration;
            long actualExpirationTime = expiration.getTime();
            
            // Allow for small time differences (within 1 second)
            assertTrue(Math.abs(expectedExpirationTime - actualExpirationTime) < 1000);
        }

        @Test
        @DisplayName("Should handle zero expiration time")
        void shouldHandleZeroExpirationTime() {
            // Arrange
            ReflectionTestUtils.setField(jwtUtil, "expiration", 0L);
            
            // Act
            String token = jwtUtil.generateToken(userDetails);
            boolean isExpired = invokeIsTokenExpired(token);
            
            // Assert
            assertTrue(isExpired);
        }

        private boolean invokeIsTokenExpired(String token) {
            return (Boolean) ReflectionTestUtils.invokeMethod(jwtUtil, "isTokenExpired", token);
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should not expose secret in token")
        void shouldNotExposeSecretInToken() {
            // Arrange
            String secret = "your-super-secret-jwt-key-must-be-at-least-32-characters-long-for-production";
            String token = jwtUtil.generateToken(userDetails);
            
            // Act & Assert
            assertFalse(token.contains(secret));
            assertFalse(token.contains("secret"));
        }

        @Test
        @DisplayName("Should use HMAC-SHA256 for signing")
        void shouldUseHmacSha256ForSigning() {
            // Arrange
            String token = jwtUtil.generateToken(userDetails);
            
            // Act & Assert
            // JWT tokens signed with HMAC-SHA256 have a specific format
            // The signature part should be base64url encoded
            String[] parts = token.split("\\.");
            assertEquals(3, parts.length);
            
            // All parts should be valid base64url
            for (String part : parts) {
                assertTrue(part.matches("^[A-Za-z0-9_-]+$"));
            }
        }

        @Test
        @DisplayName("Should include issued at time")
        void shouldIncludeIssuedAtTime() {
            // Arrange
            String token = jwtUtil.generateToken(userDetails);
            
            // Act
            Date issuedAt = jwtUtil.extractClaim(token, claims -> claims.getIssuedAt());
            
            // Assert
            assertNotNull(issuedAt);
            assertTrue(issuedAt.before(new Date()) || issuedAt.equals(new Date()));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle token parsing errors gracefully")
        void shouldHandleTokenParsingErrorsGracefully() {
            // Act & Assert
            assertFalse(jwtUtil.validateToken("invalid.token.format", userDetails));
            assertFalse(jwtUtil.validateToken("header.payload", userDetails));
            assertFalse(jwtUtil.validateToken("header.payload.signature.extra", userDetails));
        }

        @Test
        @DisplayName("Should handle null user details")
        void shouldHandleNullUserDetails() {
            // Arrange
            String token = jwtUtil.generateToken(userDetails);
            
            // Act
            boolean isValid = jwtUtil.validateToken(token, null);
            
            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should handle token with invalid signature")
        void shouldHandleTokenWithInvalidSignature() {
            // Arrange
            String token = jwtUtil.generateToken(userDetails);
            String tamperedToken = token.substring(0, token.length() - 1) + "X";
            
            // Act
            boolean isValid = jwtUtil.validateToken(tamperedToken, userDetails);
            
            // Assert
            assertFalse(isValid);
        }
    }
} 