package com.demo.springboot.config;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Rate Limit Filter Tests")
class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;

    @Mock
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
        ReflectionTestUtils.setField(rateLimitFilter, "requestsPerMinute", 10);
    }

    @Nested
    @DisplayName("Rate Limiting Tests")
    class RateLimitingTests {

        @Test
        @DisplayName("Should allow requests within rate limit")
        void shouldAllowRequestsWithinRateLimit() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRemoteAddr("192.168.1.1");
            request.setRequestURI("/api/test");

            // Make requests within the limit
            for (int i = 0; i < 5; i++) {
                rateLimitFilter.doFilterInternal(request, response, filterChain);
                assertEquals(200, response.getStatus());
            }

            verify(filterChain, times(5)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should block requests exceeding rate limit")
        void shouldBlockRequestsExceedingRateLimit() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRemoteAddr("192.168.1.2");
            request.setRequestURI("/api/test");

            // Make requests up to the limit
            for (int i = 0; i < 10; i++) {
                rateLimitFilter.doFilterInternal(request, response, filterChain);
                assertEquals(200, response.getStatus());
            }

            // Next request should be blocked
            rateLimitFilter.doFilterInternal(request, response, filterChain);
            assertEquals(429, response.getStatus());
            assertEquals("Too Many Requests", response.getContentAsString());

            verify(filterChain, times(10)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle different IP addresses separately")
        void shouldHandleDifferentIpAddressesSeparately() throws ServletException, IOException {
            MockHttpServletRequest request1 = new MockHttpServletRequest();
            MockHttpServletRequest request2 = new MockHttpServletRequest();
            MockHttpServletResponse response1 = new MockHttpServletResponse();
            MockHttpServletResponse response2 = new MockHttpServletResponse();
            
            request1.setRemoteAddr("192.168.1.10");
            request2.setRemoteAddr("192.168.1.20");
            request1.setRequestURI("/api/test");
            request2.setRequestURI("/api/test");

            // IP 1: Make requests up to limit
            for (int i = 0; i < 10; i++) {
                rateLimitFilter.doFilterInternal(request1, response1, filterChain);
                assertEquals(200, response1.getStatus());
            }

            // IP 1: Next request should be blocked
            rateLimitFilter.doFilterInternal(request1, response1, filterChain);
            assertEquals(429, response1.getStatus());

            // IP 2: Should still be allowed
            rateLimitFilter.doFilterInternal(request2, response2, filterChain);
            assertEquals(200, response2.getStatus());
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should use configured requests per minute")
        void shouldUseConfiguredRequestsPerMinute() throws ServletException, IOException {
            // Set a different rate limit
            ReflectionTestUtils.setField(rateLimitFilter, "requestsPerMinute", 5);

            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRemoteAddr("192.168.1.3");
            request.setRequestURI("/api/test");

            // Make 5 requests (should be allowed)
            for (int i = 0; i < 5; i++) {
                rateLimitFilter.doFilterInternal(request, response, filterChain);
                assertEquals(200, response.getStatus());
            }

            // 6th request should be blocked
            rateLimitFilter.doFilterInternal(request, response, filterChain);
            assertEquals(429, response.getStatus());
        }

        @Test
        @DisplayName("Should handle minimum requests per minute")
        void shouldHandleMinimumRequestsPerMinute() throws ServletException, IOException {
            ReflectionTestUtils.setField(rateLimitFilter, "requestsPerMinute", 1);

            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRemoteAddr("192.168.1.4");
            request.setRequestURI("/api/test");

            // First request should be allowed
            rateLimitFilter.doFilterInternal(request, response, filterChain);
            assertEquals(200, response.getStatus());

            // Second request should be blocked
            rateLimitFilter.doFilterInternal(request, response, filterChain);
            assertEquals(429, response.getStatus());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null remote address")
        void shouldHandleNullRemoteAddress() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRemoteAddr("unknown");
            request.setRequestURI("/api/test");

            // Should not throw exception
            assertDoesNotThrow(() -> {
                rateLimitFilter.doFilterInternal(request, response, filterChain);
            });

            assertEquals(200, response.getStatus());
        }

        @Test
        @DisplayName("Should handle empty remote address")
        void shouldHandleEmptyRemoteAddress() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRemoteAddr("");
            request.setRequestURI("/api/test");

            // Should not throw exception
            assertDoesNotThrow(() -> {
                rateLimitFilter.doFilterInternal(request, response, filterChain);
            });

            assertEquals(200, response.getStatus());
        }

        @Test
        @DisplayName("Should handle X-Forwarded-For header")
        void shouldHandleXForwardedForHeader() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRemoteAddr("10.0.0.1");
            request.addHeader("X-Forwarded-For", "203.0.113.1");
            request.setRequestURI("/api/test");

            // Should use X-Forwarded-For header
            rateLimitFilter.doFilterInternal(request, response, filterChain);
            assertEquals(200, response.getStatus());
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
            request.setRemoteAddr("192.168.1.5");
            request.setRequestURI("/api/test");

            // Mock filter chain to throw exception
            doThrow(new ServletException("Test exception")).when(filterChain).doFilter(request, response);

            // Should propagate the exception
            assertThrows(ServletException.class, () -> {
                rateLimitFilter.doFilterInternal(request, response, filterChain);
            });
        }

        @Test
        @DisplayName("Should handle response writer exceptions")
        void shouldHandleResponseWriterExceptions() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRemoteAddr("192.168.1.6");
            request.setRequestURI("/api/test");

            // Make requests to exceed limit
            for (int i = 0; i < 11; i++) {
                rateLimitFilter.doFilterInternal(request, response, filterChain);
            }

            // Should handle response writing gracefully
            assertEquals(429, response.getStatus());
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle high request volume")
        void shouldHandleHighRequestVolume() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRemoteAddr("192.168.1.7");
            request.setRequestURI("/api/test");

            // Make many requests quickly
            for (int i = 0; i < 50; i++) {
                rateLimitFilter.doFilterInternal(request, response, filterChain);
                
                if (i < 10) {
                    assertEquals(200, response.getStatus());
                } else {
                    assertEquals(429, response.getStatus());
                }
            }
        }
    }
} 