package com.demo.springboot.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(2)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // Skip logging for non-protected endpoints
        if (!requestURI.startsWith("/api/") && !requestURI.startsWith("/auth/validate")) {
            filterChain.doFilter(request, response);
            return;
        }

        logger.info("🛡️  JWT Authentication Filter - Processing {} {}", method, requestURI);
        
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            logger.info("    🔑 JWT token found in Authorization header");
            logger.debug("    🔍 Token: {}", jwt);
            
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.info("    👤 Extracted username from token: {}", username);
            } catch (Exception e) {
                logger.warn("    ❌ Invalid JWT token: {}", e.getMessage());
            }
        } else {
            logger.info("    🚫 No JWT token found in Authorization header");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.info("    🔐 Validating token for user: {}", username);
            
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            
            if (jwtUtil.validateToken(jwt, userDetails)) {
                logger.info("    ✅ Token validation successful - Setting authentication context");
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("    🎯 Authentication context set for user: {} with authorities: {}", 
                    username, userDetails.getAuthorities());
            } else {
                logger.warn("    ❌ Token validation failed for user: {}", username);
            }
        } else if (username != null) {
            logger.info("    ℹ️  User {} already authenticated in SecurityContext", username);
        }
        
        filterChain.doFilter(request, response);
    }
} 