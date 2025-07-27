package com.demo.springboot.service;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("SSRF Protected HTTP Service Tests")
class SsrfProtectedHttpServiceTest {

    private SsrfProtectedHttpService ssrfHttpService;
    
    @Mock
    private HttpClient mockHttpClient;
    
    @Mock
    private HttpResponse<String> mockHttpResponse;

    @BeforeEach
    void setUp() {
        ssrfHttpService = new SsrfProtectedHttpService();
        ReflectionTestUtils.setField(ssrfHttpService, "allowedHosts", "localhost,127.0.0.1,::1,api.github.com");
    }

    @Nested
    @DisplayName("URL Safety Validation Tests")
    class UrlSafetyValidationTests {

        @Test
        @DisplayName("Should identify safe URLs")
        void shouldIdentifySafeUrls() {
            assertTrue(ssrfHttpService.isUrlSafe("https://api.github.com/users/octocat"));
            assertTrue(ssrfHttpService.isUrlSafe("http://localhost:8080/api/test"));
            assertTrue(ssrfHttpService.isUrlSafe("https://127.0.0.1:3000/config"));
        }

        @Test
        @DisplayName("Should identify unsafe URLs")
        void shouldIdentifyUnsafeUrls() {
            assertFalse(ssrfHttpService.isUrlSafe("http://169.254.169.254/latest/meta-data/"));
            assertFalse(ssrfHttpService.isUrlSafe("http://10.0.0.1/admin"));
            assertFalse(ssrfHttpService.isUrlSafe("http://192.168.1.1/router"));
            assertFalse(ssrfHttpService.isUrlSafe("ftp://example.com/file"));
        }

        @Test
        @DisplayName("Should handle null and empty URLs")
        void shouldHandleNullAndEmptyUrls() {
            assertFalse(ssrfHttpService.isUrlSafe(null));
            assertFalse(ssrfHttpService.isUrlSafe(""));
            assertFalse(ssrfHttpService.isUrlSafe("   "));
        }
    }

    @Nested
    @DisplayName("Allowed Hosts Tests")
    class AllowedHostsTests {

        @Test
        @DisplayName("Should return configured allowed hosts")
        void shouldReturnConfiguredAllowedHosts() {
            List<String> allowedHosts = ssrfHttpService.getAllowedHosts();
            
            assertNotNull(allowedHosts);
            assertTrue(allowedHosts.contains("localhost"));
            assertTrue(allowedHosts.contains("127.0.0.1"));
            assertTrue(allowedHosts.contains("::1"));
            assertTrue(allowedHosts.contains("api.github.com"));
        }

        @Test
        @DisplayName("Should handle hosts with spaces")
        void shouldHandleHostsWithSpaces() {
            ReflectionTestUtils.setField(ssrfHttpService, "allowedHosts", " localhost , 127.0.0.1 , ::1 ");
            
            List<String> allowedHosts = ssrfHttpService.getAllowedHosts();
            
            assertTrue(allowedHosts.contains("localhost"));
            assertTrue(allowedHosts.contains("127.0.0.1"));
            assertTrue(allowedHosts.contains("::1"));
        }
    }

    @Nested
    @DisplayName("GET Request Tests")
    class GetRequestTests {

        @Test
        @DisplayName("Should throw exception for unsafe URLs")
        void shouldThrowExceptionForUnsafeUrls() {
            assertThrows(IllegalArgumentException.class, () -> {
                ssrfHttpService.get("http://169.254.169.254/latest/meta-data/");
            });
        }

        @Test
        @DisplayName("Should throw exception for non-allowed hosts")
        void shouldThrowExceptionForNonAllowedHosts() {
            assertThrows(IllegalArgumentException.class, () -> {
                ssrfHttpService.get("https://malicious-site.com/api");
            });
        }

        @Test
        @DisplayName("Should throw exception for null URL")
        void shouldThrowExceptionForNullUrl() {
            assertThrows(IllegalArgumentException.class, () -> {
                ssrfHttpService.get(null);
            });
        }

        @Test
        @DisplayName("Should throw exception for empty URL")
        void shouldThrowExceptionForEmptyUrl() {
            assertThrows(IllegalArgumentException.class, () -> {
                ssrfHttpService.get("");
            });
        }
    }

    @Nested
    @DisplayName("POST Request Tests")
    class PostRequestTests {

        @Test
        @DisplayName("Should throw exception for unsafe URLs")
        void shouldThrowExceptionForUnsafeUrls() {
            assertThrows(IllegalArgumentException.class, () -> {
                ssrfHttpService.post("http://169.254.169.254/latest/meta-data/", "data", "application/json");
            });
        }

        @Test
        @DisplayName("Should throw exception for non-allowed hosts")
        void shouldThrowExceptionForNonAllowedHosts() {
            assertThrows(IllegalArgumentException.class, () -> {
                ssrfHttpService.post("https://malicious-site.com/api", "data", "application/json");
            });
        }

        @Test
        @DisplayName("Should throw exception for null URL")
        void shouldThrowExceptionForNullUrl() {
            assertThrows(IllegalArgumentException.class, () -> {
                ssrfHttpService.post(null, "data", "application/json");
            });
        }

        @Test
        @DisplayName("Should throw exception for empty URL")
        void shouldThrowExceptionForEmptyUrl() {
            assertThrows(IllegalArgumentException.class, () -> {
                ssrfHttpService.post("", "data", "application/json");
            });
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should handle empty allowed hosts")
        void shouldHandleEmptyAllowedHosts() {
            ReflectionTestUtils.setField(ssrfHttpService, "allowedHosts", "");
            
            List<String> allowedHosts = ssrfHttpService.getAllowedHosts();
            assertNotNull(allowedHosts);
            assertTrue(allowedHosts.isEmpty());
        }

        @Test
        @DisplayName("Should handle single allowed host")
        void shouldHandleSingleAllowedHost() {
            ReflectionTestUtils.setField(ssrfHttpService, "allowedHosts", "localhost");
            
            List<String> allowedHosts = ssrfHttpService.getAllowedHosts();
            assertEquals(1, allowedHosts.size());
            assertEquals("localhost", allowedHosts.get(0));
        }

        @Test
        @DisplayName("Should handle multiple allowed hosts")
        void shouldHandleMultipleAllowedHosts() {
            ReflectionTestUtils.setField(ssrfHttpService, "allowedHosts", "host1,host2,host3");
            
            List<String> allowedHosts = ssrfHttpService.getAllowedHosts();
            assertEquals(3, allowedHosts.size());
            assertTrue(allowedHosts.contains("host1"));
            assertTrue(allowedHosts.contains("host2"));
            assertTrue(allowedHosts.contains("host3"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle malformed URLs gracefully")
        void shouldHandleMalformedUrlsGracefully() {
            assertFalse(ssrfHttpService.isUrlSafe("not-a-url"));
            assertFalse(ssrfHttpService.isUrlSafe("http://"));
            assertFalse(ssrfHttpService.isUrlSafe("://example.com"));
        }

        @Test
        @DisplayName("Should handle URLs with invalid schemes")
        void shouldHandleUrlsWithInvalidSchemes() {
            assertFalse(ssrfHttpService.isUrlSafe("ftp://example.com/file"));
            assertFalse(ssrfHttpService.isUrlSafe("file:///etc/passwd"));
            assertFalse(ssrfHttpService.isUrlSafe("gopher://example.com"));
        }

        @Test
        @DisplayName("Should handle private IP ranges")
        void shouldHandlePrivateIpRanges() {
            assertFalse(ssrfHttpService.isUrlSafe("http://10.0.0.1/admin"));
            assertFalse(ssrfHttpService.isUrlSafe("https://172.16.0.1/config"));
            assertFalse(ssrfHttpService.isUrlSafe("http://192.168.1.1/router"));
        }

        @Test
        @DisplayName("Should handle loopback addresses")
        void shouldHandleLoopbackAddresses() {
            assertFalse(ssrfHttpService.isUrlSafe("http://127.0.0.1:8080/admin"));
            assertFalse(ssrfHttpService.isUrlSafe("http://0.0.0.0:8080"));
            assertFalse(ssrfHttpService.isUrlSafe("http://[::1]:8080/api"));
        }

        @Test
        @DisplayName("Should handle cloud metadata services")
        void shouldHandleCloudMetadataServices() {
            assertFalse(ssrfHttpService.isUrlSafe("http://169.254.169.254/latest/meta-data/"));
            assertFalse(ssrfHttpService.isUrlSafe("http://metadata.google.internal/computeMetadata/v1/"));
        }

        @Test
        @DisplayName("Should handle Docker internal addresses")
        void shouldHandleDockerInternalAddresses() {
            assertFalse(ssrfHttpService.isUrlSafe("http://host.docker.internal:8080/admin"));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should validate complete URL validation flow")
        void shouldValidateCompleteUrlValidationFlow() {
            // Test safe URLs
            assertTrue(ssrfHttpService.isUrlSafe("https://api.github.com/users/octocat"));
            assertTrue(ssrfHttpService.isUrlSafe("http://localhost:8080/api/test"));
            
            // Test unsafe URLs
            assertFalse(ssrfHttpService.isUrlSafe("http://169.254.169.254/latest/meta-data/"));
            assertFalse(ssrfHttpService.isUrlSafe("http://10.0.0.1/admin"));
            assertFalse(ssrfHttpService.isUrlSafe("ftp://example.com/file"));
            
            // Test edge cases
            assertFalse(ssrfHttpService.isUrlSafe(null));
            assertFalse(ssrfHttpService.isUrlSafe(""));
            assertFalse(ssrfHttpService.isUrlSafe("not-a-url"));
        }

        @Test
        @DisplayName("Should handle mixed case in allowed hosts")
        void shouldHandleMixedCaseInAllowedHosts() {
            ReflectionTestUtils.setField(ssrfHttpService, "allowedHosts", "LocalHost,127.0.0.1,API.GITHUB.COM");
            
            List<String> allowedHosts = ssrfHttpService.getAllowedHosts();
            assertTrue(allowedHosts.contains("localhost"));
            assertTrue(allowedHosts.contains("127.0.0.1"));
            assertTrue(allowedHosts.contains("api.github.com"));
        }
    }
} 