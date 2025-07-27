package com.demo.springboot.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.demo.springboot.config.SsrfProtectionFilter;

/**
 * SSRF-Protected HTTP Service
 * Provides safe methods for making external HTTP requests with SSRF protection
 */
@Service
public class SsrfProtectedHttpService {

    private static final Logger logger = LoggerFactory.getLogger(SsrfProtectedHttpService.class);

    @Value("${security.ssrf.allowed-hosts:localhost,127.0.0.1,::1}")
    private String allowedHosts;

    private final HttpClient httpClient;

    public SsrfProtectedHttpService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Make a safe GET request to an external URL
     * @param url The URL to request
     * @return The response body as a string
     * @throws IOException if the request fails
     * @throws IllegalArgumentException if the URL is not allowed
     */
    public String get(String url) throws IOException, IllegalArgumentException {
        validateUrl(url);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            logger.info("🛡️ SSRF-Protected GET request to {} returned status: {}", url, response.statusCode());
            
            if (response.statusCode() >= 400) {
                throw new IOException("HTTP request failed with status: " + response.statusCode());
            }
            
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request was interrupted", e);
        }
    }

    /**
     * Make a safe POST request to an external URL
     * @param url The URL to request
     * @param body The request body
     * @param contentType The content type
     * @return The response body as a string
     * @throws IOException if the request fails
     * @throws IllegalArgumentException if the URL is not allowed
     */
    public String post(String url, String body, String contentType) throws IOException, IllegalArgumentException {
        validateUrl(url);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", contentType)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            logger.info("🛡️ SSRF-Protected POST request to {} returned status: {}", url, response.statusCode());
            
            if (response.statusCode() >= 400) {
                throw new IOException("HTTP request failed with status: " + response.statusCode());
            }
            
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request was interrupted", e);
        }
    }

    /**
     * Validate if a URL is allowed for external requests
     * @param url The URL to validate
     * @throws IllegalArgumentException if the URL is not allowed
     */
    private void validateUrl(String url) throws IllegalArgumentException {
        if (!SsrfProtectionFilter.validateUrlForExternalRequest(url)) {
            logger.error("🚫 SSRF Protection: Blocked external request to: {}", url);
            throw new IllegalArgumentException("URL not allowed for external requests: " + url);
        }

        // Additional validation for allowed hosts
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            
            if (host != null && !isHostAllowed(host)) {
                logger.error("🚫 SSRF Protection: Host not in allowed list: {}", host);
                throw new IllegalArgumentException("Host not allowed: " + host);
            }
        } catch (Exception e) {
            logger.error("🚫 SSRF Protection: Invalid URL format: {}", url);
            throw new IllegalArgumentException("Invalid URL format: " + url);
        }
    }

    /**
     * Check if a host is in the allowed hosts list
     * @param host The host to check
     * @return true if the host is allowed
     */
    private boolean isHostAllowed(String host) {
        List<String> allowedHostList = List.of(allowedHosts.split(","));
        return allowedHostList.stream()
                .map(String::trim)
                .anyMatch(host.toLowerCase()::equals);
    }

    /**
     * Get the list of allowed hosts
     * @return List of allowed hosts
     */
    public List<String> getAllowedHosts() {
        return List.of(allowedHosts.split(","));
    }

    /**
     * Check if a URL is safe for external requests (without throwing exception)
     * @param url The URL to check
     * @return true if the URL is safe
     */
    public boolean isUrlSafe(String url) {
        try {
            validateUrl(url);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
} 