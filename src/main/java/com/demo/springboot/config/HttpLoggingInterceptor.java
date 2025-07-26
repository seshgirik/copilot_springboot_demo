package com.demo.springboot.config;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP Request/Response Logging Interceptor
 * Logs detailed information about every HTTP REST API call
 */
@Component
public class HttpLoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpLoggingInterceptor.class);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        
        logger.info("🌐 =============== HTTP REQUEST ===============");
        logger.info("🔗 Method: {} {}", request.getMethod(), request.getRequestURI());
        logger.info("🌍 Remote Address: {}", request.getRemoteAddr());
        logger.info("🔧 User-Agent: {}", request.getHeader("User-Agent"));
        logger.info("📋 Content-Type: {}", request.getContentType());
        
        // Log query parameters
        if (request.getQueryString() != null) {
            logger.info("❓ Query String: {}", request.getQueryString());
        }
        
        // Log headers
        logger.info("📤 Request Headers:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            logger.info("   {} = {}", headerName, request.getHeader(headerName));
        }
        
        // Store start time for response logging
        request.setAttribute("startTime", System.currentTimeMillis());
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;
        
        logger.info("🌐 =============== HTTP RESPONSE ==============");
        logger.info("📊 Status: {} ({})", response.getStatus(), getStatusText(response.getStatus()));
        logger.info("⏱️  Duration: {} ms", duration);
        logger.info("📋 Content-Type: {}", response.getContentType());
        
        // Log response headers
        logger.info("📤 Response Headers:");
        for (String headerName : response.getHeaderNames()) {
            logger.info("   {} = {}", headerName, response.getHeader(headerName));
        }
        
        if (ex != null) {
            logger.error("❌ Exception occurred: {}", ex.getMessage(), ex);
        }
        
        logger.info("🌐 ============================================");
    }
    
    private String getStatusText(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }
}
