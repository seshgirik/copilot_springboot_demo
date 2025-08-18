package com.demo.springboot.config;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
@DisplayName("SSRF Protection Filter Tests")
class SsrfProtectionFilterTest {

    private SsrfProtectionFilter ssrfFilter;
    
    @Mock
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        ssrfFilter = new SsrfProtectionFilter();
        ReflectionTestUtils.setField(ssrfFilter, "allowedSchemes", "http,https");
        ReflectionTestUtils.setField(ssrfFilter, "allowedHosts", "localhost,127.0.0.1,::1");
    }

    @Nested
    @DisplayName("URL Validation Tests")
    class UrlValidationTests {

        @Test
        @DisplayName("Should validate safe external URLs")
        void shouldValidateSafeExternalUrls() {
            System.out.println("START: SsrfProtectionFilterTest.UrlValidationTests.shouldValidateSafeExternalUrls");
            assertTrue(SsrfProtectionFilter.validateUrlForExternalRequest("https://api.github.com/users/octocat"));
            assertTrue(SsrfProtectionFilter.validateUrlForExternalRequest("http://example.com/api/data"));
            assertTrue(SsrfProtectionFilter.validateUrlForExternalRequest("https://trusted-service.com/webhook"));
            System.out.println("END: SsrfProtectionFilterTest.UrlValidationTests.shouldValidateSafeExternalUrls");
        }

        @Test
        @DisplayName("Should block private IP ranges")
        void shouldBlockPrivateIpRanges() {
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://10.0.0.1/admin"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("https://172.16.0.1/config"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://192.168.1.1/router"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("https://172.31.255.255/api"));
        }

        @Test
        @DisplayName("Should block loopback addresses")
        void shouldBlockLoopbackAddresses() {
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://127.0.0.1:8080/admin"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("https://localhost:3000/config"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://0.0.0.0:8080"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://[::1]:8080/api"));
        }

        @Test
        @DisplayName("Should block cloud metadata services")
        void shouldBlockCloudMetadataServices() {
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://169.254.169.254/latest/meta-data/"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://metadata.google.internal/computeMetadata/v1/"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://169.254.169.254/metadata/instance"));
        }

        @Test
        @DisplayName("Should block Docker internal addresses")
        void shouldBlockDockerInternalAddresses() {
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://host.docker.internal:8080/admin"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("https://host.docker.internal/api/config"));
        }

        @Test
        @DisplayName("Should block link-local addresses")
        void shouldBlockLinkLocalAddresses() {
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://169.254.1.1/config"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://[fe80::1]/admin"));
        }

        @Test
        @DisplayName("Should block documentation/example addresses")
        void shouldBlockDocumentationAddresses() {
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://0.0.0.0:8080"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://203.0.113.1/api"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://198.51.100.1/config"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://198.18.0.1/admin"));
        }

        @Test
        @DisplayName("Should block non-HTTP/HTTPS schemes")
        void shouldBlockNonHttpSchemes() {
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("ftp://example.com/file"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("file:///etc/passwd"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("gopher://example.com"));
        }

        @Test
        @DisplayName("Should handle null and empty URLs")
        void shouldHandleNullAndEmptyUrls() {
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest(null));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest(""));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("   "));
        }

        @Test
        @DisplayName("Should handle malformed URLs")
        void shouldHandleMalformedUrls() {
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("not-a-url"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("http://"));
            assertFalse(SsrfProtectionFilter.validateUrlForExternalRequest("://example.com"));
        }
    }

    @Nested
    @DisplayName("Request Filtering Tests")
    class RequestFilteringTests {

        @Test
        @DisplayName("Should allow requests without URL parameters")
        void shouldAllowRequestsWithoutUrlParameters() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/test");
            request.setMethod("GET");

            ssrfFilter.doFilterInternal(request, response, filterChain);

            assertEquals(200, response.getStatus());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should block requests with malicious URL parameters")
        void shouldBlockRequestsWithMaliciousUrlParameters() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/test");
            request.setMethod("GET");
            request.setQueryString("url=http://169.254.169.254/latest/meta-data/");

            ssrfFilter.doFilterInternal(request, response, filterChain);

            assertEquals(400, response.getStatus());
            assertEquals("Invalid URL parameter detected", response.getContentAsString());
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("Should allow requests with safe URL parameters")
        void shouldAllowRequestsWithSafeUrlParameters() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/test");
            request.setMethod("GET");
            request.setQueryString("url=https://api.github.com/users/octocat");

            ssrfFilter.doFilterInternal(request, response, filterChain);

            assertEquals(200, response.getStatus());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle multiple URL parameters")
        void shouldHandleMultipleUrlParameters() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/test");
            request.setMethod("GET");
            request.setQueryString("url=https://api.github.com/users/octocat&redirect=http://127.0.0.1/admin");

            ssrfFilter.doFilterInternal(request, response, filterChain);

            assertEquals(400, response.getStatus());
            assertEquals("Invalid URL parameter detected", response.getContentAsString());
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("Should handle URL-encoded parameters")
        void shouldHandleUrlEncodedParameters() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/test");
            request.setMethod("GET");
            request.setQueryString("url=http%3A%2F%2F169.254.169.254%2Flatest%2Fmeta-data%2F");

            ssrfFilter.doFilterInternal(request, response, filterChain);

            assertEquals(400, response.getStatus());
            assertEquals("Invalid URL parameter detected", response.getContentAsString());
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("Should detect various URL parameter names")
        void shouldDetectVariousUrlParameterNames() throws ServletException, IOException {
            String[] urlParamNames = {"url", "uri", "link", "href", "src", "redirect", "callback", "return", "next"};
            
            for (String paramName : urlParamNames) {
                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();
                request.setRequestURI("/api/test");
                request.setMethod("GET");
                request.setQueryString(paramName + "=http://127.0.0.1/admin");

                ssrfFilter.doFilterInternal(request, response, filterChain);

                assertEquals(400, response.getStatus(), "Should block parameter: " + paramName);
                assertEquals("Invalid URL parameter detected", response.getContentAsString());
            }
        }

        @Test
        @DisplayName("Should handle JSON content type requests")
        void shouldHandleJsonContentTypeRequests() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/test");
            request.setMethod("POST");
            request.setContentType("application/json");

            ssrfFilter.doFilterInternal(request, response, filterChain);

            assertEquals(200, response.getStatus());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle null query string")
        void shouldHandleNullQueryString() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/test");
            request.setMethod("GET");
            request.setQueryString(null);

            ssrfFilter.doFilterInternal(request, response, filterChain);

            assertEquals(200, response.getStatus());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle empty query string")
        void shouldHandleEmptyQueryString() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/test");
            request.setMethod("GET");
            request.setQueryString("");

            ssrfFilter.doFilterInternal(request, response, filterChain);

            assertEquals(200, response.getStatus());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("Should handle filter chain exceptions")
        void shouldHandleFilterChainExceptions() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/test");
            request.setMethod("GET");

            doThrow(new ServletException("Test exception")).when(filterChain).doFilter(any(), any());

            assertThrows(ServletException.class, () -> {
                ssrfFilter.doFilterInternal(request, response, filterChain);
            });
        }

        @Test
        @DisplayName("Should handle response writer exceptions")
        void shouldHandleResponseWriterExceptions() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = mock(MockHttpServletResponse.class);
            request.setRequestURI("/api/test");
            request.setMethod("GET");
            request.setQueryString("url=http://127.0.0.1/admin");

            when(response.getWriter()).thenThrow(new IOException("Writer exception"));

            assertThrows(IOException.class, () -> {
                ssrfFilter.doFilterInternal(request, response, filterChain);
            });
        }
    }
} 