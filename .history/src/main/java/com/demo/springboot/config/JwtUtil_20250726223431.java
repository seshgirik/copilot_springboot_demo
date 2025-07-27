package com.demo.springboot.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long expiration;

    private SecretKey getSigningKey() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be set and at least 32 characters long. Set 'jwt.secret' in your environment or configuration.");
        }
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    public String generateToken(UserDetails userDetails, String role) {
        logger.info("    🔐 JWT Token Generation Process Started");
        logger.info("    📋 Input Parameters:");
        logger.info("        - Username: {}", userDetails.getUsername());
        logger.info("        - Role: {}", role);
        logger.info("        - Authorities: {}", userDetails.getAuthorities());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        logger.info("    📝 Claims prepared: {}", claims);
        
        logger.info("    🏗️  Building JWT token with claims and user information");
        String token = createToken(claims, userDetails.getUsername());
        
        logger.info("    ✅ JWT Token generated successfully");
        logger.info("    📊 Token Details:");
        logger.info("        - Subject (username): {}", userDetails.getUsername());
        logger.info("        - Role claim: {}", role);
        logger.info("        - Expiration time: {} hours", expiration / (1000 * 60 * 60));
        logger.debug("    🔑 Generated Token: {}", token);
        
        return token;
    }

    private String createToken(Map<String, Object> claims, String subject) {
        logger.debug("        🔨 Creating JWT token with HMAC-SHA256 signature");
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expirationDate = new Date(System.currentTimeMillis() + expiration);
        
        logger.debug("        📅 Token timestamps:");
        logger.debug("            - Issued at: {}", issuedAt);
        logger.debug("            - Expires at: {}", expirationDate);
        
        String token = Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
                
        logger.debug("        🔐 Token signed and compacted successfully");
        return token;
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        logger.debug("        🔍 Validating JWT token for user: {}", userDetails.getUsername());
        
        try {
            final String username = extractUsername(token);
            boolean isUsernameValid = username.equals(userDetails.getUsername());
            boolean isTokenNotExpired = !isTokenExpired(token);
            
            logger.debug("        📋 Token validation details:");
            logger.debug("            - Token username: {}", username);
            logger.debug("            - UserDetails username: {}", userDetails.getUsername());
            logger.debug("            - Username match: {}", isUsernameValid);
            logger.debug("            - Token not expired: {}", isTokenNotExpired);
            
            boolean isValid = isUsernameValid && isTokenNotExpired;
            
            if (isValid) {
                logger.debug("        ✅ Token validation successful");
            } else {
                logger.debug("        ❌ Token validation failed");
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("        ❌ Token validation error: {}", e.getMessage());
            return false;
        }
    }

    public String extractRole(String token) {
        logger.debug("        🏷️  Extracting role from token");
        try {
            Claims claims = extractAllClaims(token);
            String role = claims.get("role", String.class);
            logger.debug("        🏷️  Extracted role: {}", role);
            return role;
        } catch (Exception e) {
            logger.error("        ❌ Error extracting role from token: {}", e.getMessage());
            return null;
        }
    }
} 