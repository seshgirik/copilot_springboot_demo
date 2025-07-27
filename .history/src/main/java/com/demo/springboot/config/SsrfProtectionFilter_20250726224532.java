package com.demo.springboot.config;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * SSRF Protection Filter
 * Prevents Server-Side Request Forgery attacks by validating URLs and restricting external requests
 */
@Component
@Order(3)
public class SsrfProtectionFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SsrfProtectionFilter.class);

    @Value("${security.ssrf.allowed-hosts:localhost,127.0.0.1,::1}")
    private String allowedHosts;

    @Value("${security.ssrf.allowed-schemes:http,https}")
    private String allowedSchemes;

    // Patterns for detecting SSRF attempts
    private static final List<Pattern> SSRF_PATTERNS = Arrays.asList(
        // Private IP ranges
        Pattern.compile("^(10\\.|172\\.(1[6-9]|2[0-9]|3[01])\\.|192\\.168\\.)"),
        // Loopback addresses
        Pattern.compile("^(127\\.|0\\.0\\.0|::1)"),
        // Link-local addresses
        Pattern.compile("^(169\\.254\\.|fe80:)"),
        // Documentation/example addresses
        Pattern.compile("^(0\\.|203\\.0\\.113\\.|198\\.51\\.100\\.|198\\.18\\.)"),
        // AWS metadata service
        Pattern.compile("^169\\.254\\.169\\.254"),
        // Google metadata service
        Pattern.compile("^metadata\\.google\\.internal"),
        // Azure metadata service
        Pattern.compile("^169\\.254\\.169\\.254"),
        // Docker internal
        Pattern.compile("^host\\.docker\\.internal")
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        
        // Check for URL parameters that might contain external URLs
        if (queryString != null && containsUrlParameters(queryString)) {
            logger.warn("🛡️ SSRF Protection: Detected potential URL parameter in query string");
            
            // Extract and validate any URL parameters
            String[] params = queryString.split("&");
            for (String param : params) {
                if (param.contains("=")) {
                    String[] keyValue = param.split("=", 2);
                    if (keyValue.length == 2 && isUrlParameter(keyValue[0])) {
                        String urlValue = keyValue[1];
                        if (!isValidUrl(urlValue)) {
                            logger.error("🚫 SSRF Protection: Blocked malicious URL in parameter {}: {}", keyValue[0], urlValue);
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            response.getWriter().write("Invalid URL parameter detected");
                            return;
                        }
                    }
                }
            }
        }

        // Check request body for URLs (if content type suggests JSON)
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            // Note: For full body scanning, you'd need to wrap the request
            // This is a simplified check - in production, consider using a request wrapper
            logger.debug("🛡️ SSRF Protection: JSON content detected, consider body scanning for URLs");
        }

        filterChain.doFilter(request, response);
    }

    private boolean containsUrlParameters(String queryString) {
        String[] urlParamNames = {"url", "uri", "link", "href", "src", "redirect", "callback", "return", "next"};
        String lowerQuery = queryString.toLowerCase();
        return Arrays.stream(urlParamNames).anyMatch(lowerQuery::contains);
    }

    private boolean isUrlParameter(String paramName) {
        String[] urlParamNames = {"url", "uri", "link", "href", "src", "redirect", "callback", "return", "next"};
        return Arrays.stream(urlParamNames).anyMatch(paramName.toLowerCase()::equals);
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            // Decode URL if it's encoded
            String decodedUrl = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8);
            
            // Check against SSRF patterns
            for (Pattern pattern : SSRF_PATTERNS) {
                if (pattern.matcher(decodedUrl).find()) {
                    logger.warn("🚫 SSRF Protection: Blocked URL matching SSRF pattern: {}", decodedUrl);
                    return false;
                }
            }

            // Parse the URL
            URI uri = new URI(decodedUrl);
            
            // Validate scheme
            String scheme = uri.getScheme();
            if (scheme == null || !isAllowedScheme(scheme)) {
                logger.warn("🚫 SSRF Protection: Blocked URL with disallowed scheme: {}", scheme);
                return false;
            }

            // Validate host
            String host = uri.getHost();
            if (host == null || !isAllowedHost(host)) {
                logger.warn("🚫 SSRF Protection: Blocked URL with disallowed host: {}", host);
                return false;
            }

            return true;
        } catch (URISyntaxException | IllegalArgumentException e) {
            logger.warn("🚫 SSRF Protection: Invalid URL format: {}", url);
            return false;
        }
    }

    private boolean isAllowedScheme(String scheme) {
        String[] allowed = allowedSchemes.split(",");
        return Arrays.stream(allowed).anyMatch(scheme.toLowerCase()::equals);
    }

    private boolean isAllowedHost(String host) {
        String[] allowed = allowedHosts.split(",");
        return Arrays.stream(allowed).anyMatch(host.toLowerCase()::equals);
    }

    /**
     * Utility method to validate URLs before making external requests
     * Use this method in your services when making HTTP requests
     */
    public static boolean validateUrlForExternalRequest(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            URI uri = new URI(url);
            
            // Only allow HTTP and HTTPS
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return false;
            }

            // Check against SSRF patterns
            for (Pattern pattern : SSRF_PATTERNS) {
                if (pattern.matcher(url).find()) {
                    return false;
                }
            }

            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
} 