package com.demo.springboot.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP Request/Response Body Logging Filter
 * Captures and logs the actual request and response bodies
 */
@Component
public class HttpBodyLoggingFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpBodyLoggingFilter.class);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Skip logging for certain paths
        if (shouldSkipLogging(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Wrap request and response to capture bodies
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        
        try {
            // Process the request
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // Log request body
            logRequestBody(wrappedRequest);
            
            // Log response body
            logResponseBody(wrappedResponse);
            
            // Important: Copy response body back to the response
            wrappedResponse.copyBodyToResponse();
        }
    }
    
    private void logRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            logger.info("📥 Request Body:\n{}", formatJson(body));
        } else {
            logger.info("📥 Request Body: (empty)");
        }
    }
    
    private void logResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            logger.info("📤 Response Body:\n{}", formatJson(body));
        } else {
            logger.info("📤 Response Body: (empty)");
        }
    }
    
    private String formatJson(String json) {
        // Simple JSON formatting - in production, you might want to use a proper JSON formatter
        if (json.trim().startsWith("{") || json.trim().startsWith("[")) {
            return json; // Return as-is for now
        }
        return json;
    }
    
    private boolean shouldSkipLogging(String uri) {
        return uri.contains("/actuator") || 
               uri.contains("/h2-console") || 
               uri.contains("/swagger") ||
               uri.contains("/favicon.ico") ||
               uri.contains("/css/") ||
               uri.contains("/js/") ||
               uri.contains("/images/");
    }
}
