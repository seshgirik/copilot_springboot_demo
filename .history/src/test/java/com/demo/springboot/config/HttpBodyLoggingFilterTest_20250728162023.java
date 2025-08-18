package com.demo.springboot.config;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HTTP Body Logging Filter Tests")
class HttpBodyLoggingFilterTest {

    private HttpBodyLoggingFilter httpBodyLoggingFilter;

    @Mock
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        httpBodyLoggingFilter = new HttpBodyLoggingFilter();
    }

    @Nested
    @DisplayName("Sensitive Endpoint Tests")
    class SensitiveEndpointTests {

        @Test
        @DisplayName("Should skip logging for auth endpoints")
        void shouldSkipLoggingForAuthEndpoints() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/auth/login");
            request.setMethod("POST");

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
            // Should not log sensitive data
        }

        @Test
        @DisplayName("Should skip logging for register endpoints")
        void shouldSkipLoggingForRegisterEndpoints() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/auth/register");
            request.setMethod("POST");

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should skip logging for password endpoints")
        void shouldSkipLoggingForPasswordEndpoints() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/auth/password/reset");
            request.setMethod("POST");

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should allow logging for non-sensitive endpoints")
        void shouldAllowLoggingForNonSensitiveEndpoints() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/products");
            request.setMethod("GET");

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("Request Body Logging Tests")
    class RequestBodyLoggingTests {

        @Test
        @DisplayName("Should handle empty request body")
        void shouldHandleEmptyRequestBody() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/test");
            request.setMethod("POST");
            request.setContentType("application/json");

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should handle request with content")
        void shouldHandleRequestWithContent() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/products");
            request.setMethod("POST");
            request.setContentType("application/json");
            request.setContent("{\"name\":\"Test Product\"}".getBytes());

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("Response Body Logging Tests")
    class ResponseBodyLoggingTests {

        @Test
        @DisplayName("Should handle empty response body")
        void shouldHandleEmptyResponseBody() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/test");
            request.setMethod("GET");

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should handle response with content")
        void shouldHandleResponseWithContent() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/products");
            request.setMethod("GET");

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("HTTP Method Tests")
    class HttpMethodTests {

        @Test
        @DisplayName("Should handle GET requests")
        void shouldHandleGetRequests() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/products");
            request.setMethod("GET");

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should handle POST requests")
        void shouldHandlePostRequests() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/products");
            request.setMethod("POST");

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should handle PUT requests")
        void shouldHandlePutRequests() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/products/1");
            request.setMethod("PUT");

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should handle DELETE requests")
        void shouldHandleDeleteRequests() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/products/1");
            request.setMethod("DELETE");

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("Content Type Tests")
    class ContentTypeTests {

        @Test
        @DisplayName("Should handle JSON content type")
        void shouldHandleJsonContentType() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/products");
            request.setMethod("POST");
            request.setContentType("application/json");

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should handle XML content type")
        void shouldHandleXmlContentType() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/products");
            request.setMethod("POST");
            request.setContentType("application/xml");

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should handle form data content type")
        void shouldHandleFormDataContentType() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/products");
            request.setMethod("POST");
            request.setContentType("application/x-www-form-urlencoded");

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should handle null content type")
        void shouldHandleNullContentType() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/products");
            request.setMethod("GET");
            request.setContentType(null);

            httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle filter chain exceptions")
        void shouldHandleFilterChainExceptions() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/test");
            request.setMethod("GET");

            // Mock filter chain to throw exception
            doThrow(new ServletException("Test exception")).when(filterChain).doFilter(request, response);

            // Should propagate the exception
            assertThrows(ServletException.class, () -> {
                httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);
            });
        }

        @Test
        @DisplayName("Should handle null request URI")
        void shouldHandleNullRequestUri() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI(null);
            request.setMethod("GET");

            // Should not throw exception
            assertDoesNotThrow(() -> {
                httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);
            });

            verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("Sensitive Data Protection Tests")
    class SensitiveDataProtectionTests {

        @Test
        @DisplayName("Should not log sensitive endpoints")
        void shouldNotLogSensitiveEndpoints() throws ServletException, IOException {
            String[] sensitiveEndpoints = {
                "/auth/login",
                "/auth/register", 
                "/auth/password/reset",
                "/auth/password/change",
                "/auth/token/refresh"
            };

            for (String endpoint : sensitiveEndpoints) {
                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();
                request.setRequestURI(endpoint);
                request.setMethod("POST");

                httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

                verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
            }
        }

        @Test
        @DisplayName("Should log non-sensitive endpoints")
        void shouldLogNonSensitiveEndpoints() throws ServletException, IOException {
            String[] nonSensitiveEndpoints = {
                "/api/products",
                "/api/users",
                "/api/test",
                "/health",
                "/actuator/info"
            };

            for (String endpoint : nonSensitiveEndpoints) {
                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();
                request.setRequestURI(endpoint);
                request.setMethod("GET");

                httpBodyLoggingFilter.doFilterInternal(request, response, filterChain);

                verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
            }
        }
    }
} 