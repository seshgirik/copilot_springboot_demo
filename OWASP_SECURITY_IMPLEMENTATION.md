# OWASP Top 10 Security Implementation Guide

This document details the comprehensive security measures implemented in the Spring Boot application to protect against OWASP Top 10 (2021) threats.

## 1. Broken Access Control

### Implementation
- **Role-based Access Control (RBAC)**: Implemented in `SecurityConfig.java`
- **Method-level Security**: Enabled with `@EnableMethodSecurity`
- **URL-level Security**: Configured in security filter chain

### Code Examples
```java
// SecurityConfig.java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**", "/debug/**", "/api/test/rate-limit").permitAll()
    .requestMatchers("/h2-console/**", "/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").hasRole("ADMIN")
    .requestMatchers("/api/products/**").hasAnyRole("USER", "ADMIN")
    .requestMatchers("/api/users/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

### Best Practices
- Use `@PreAuthorize` annotations for method-level security
- Implement principle of least privilege
- Regular access control audits

## 2. Cryptographic Failures

### Implementation
- **Password Hashing**: BCrypt with configurable strength
- **JWT Security**: HMAC-SHA256 signing with strong secret
- **Secret Management**: Environment-based configuration

### Code Examples
```java
// JwtUtil.java
@Value("${jwt.secret}")
private String secret;

private SecretKey getSigningKey() {
    if (secret == null || secret.length() < 32) {
        throw new IllegalStateException("JWT secret must be set and at least 32 characters long");
    }
    return Keys.hmacShaKeyFor(secret.getBytes());
}
```

### Configuration
```yaml
jwt:
  secret: ${JWT_SECRET:your-super-secret-jwt-key-must-be-at-least-32-characters-long-for-production}
  expiration: 86400000 # 24 hours
```

### Best Practices
- Use strong, randomly generated secrets
- Rotate secrets regularly
- Use secrets management services in production

## 3. Injection

### Implementation
- **SQL Injection Protection**: Spring Data JPA parameterized queries
- **Input Validation**: Bean Validation annotations
- **Query Sanitization**: Avoid dynamic query construction

### Code Examples
```java
// UserRepository.java - Safe parameterized queries
@Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
List<User> findByNameContainingIgnoreCase(@Param("name") String name);
```

### Best Practices
- Always use parameterized queries
- Validate and sanitize all inputs
- Avoid string concatenation in queries

## 4. Insecure Design

### Implementation
- **Secure Authentication Flow**: Custom authentication provider
- **Error Handling**: Proper exception handling without information disclosure
- **Threat Modeling**: Security-first design approach

### Code Examples
```java
// CustomAuthenticationProvider.java
@Override
public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    // Secure authentication logic with proper error handling
    try {
        // Authentication logic
    } catch (Exception e) {
        logger.error("Authentication error occurred: {}", e.getMessage());
        throw new BadCredentialsException("Authentication failed");
    }
}
```

### Best Practices
- Design with security in mind from the start
- Regular security reviews
- Follow secure coding guidelines

## 5. Security Misconfiguration

### Implementation
- **Secure HTTP Headers**: CSP, HSTS, X-Frame-Options
- **CORS Configuration**: Restricted origins
- **Development Tools Protection**: H2 console and Swagger UI secured

### Code Examples
```java
// SecurityConfig.java
.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self'; object-src 'none';"))
    .frameOptions(frame -> frame.sameOrigin())
    .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
    .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.SAME_ORIGIN))
    .contentTypeOptions(withDefaults -> {})
)
```

### Configuration
```yaml
security:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:https://yourdomain.com}
```

### Best Practices
- Disable unnecessary features in production
- Use security headers
- Regular configuration audits

## 6. Vulnerable and Outdated Components

### Implementation
- **Dependency Management**: Maven with version control
- **Regular Updates**: Automated dependency checking recommended

### Best Practices
- Use OWASP Dependency-Check
- Regular dependency updates
- Monitor security advisories

## 7. Identification and Authentication Failures

### Implementation
- **Strong Password Policy**: Configurable complexity requirements
- **Account Lockout**: After failed attempts with configurable duration
- **JWT Token Management**: Proper expiration and validation

### Code Examples
```java
// AuthController.java
private boolean isPasswordStrong(String password) {
    if (password == null || password.length() < minPasswordLength) return false;
    boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
    boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
    boolean hasDigit = password.chars().anyMatch(Character::isDigit);
    boolean hasSpecial = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
    return hasUpper && hasLower && hasDigit && hasSpecial;
}
```

### Database Schema
```sql
-- Enhanced User entity with security fields
ALTER TABLE users ADD COLUMN failed_login_attempts INT DEFAULT 0;
ALTER TABLE users ADD COLUMN account_non_locked BOOLEAN DEFAULT TRUE;
ALTER TABLE users ADD COLUMN lock_time TIMESTAMP NULL;
```

### Configuration
```yaml
security:
  account-lockout:
    max-attempts: 5
    lock-duration-minutes: 15
  password:
    min-length: 8
```

### Best Practices
- Enforce strong password policies
- Implement account lockout
- Use multi-factor authentication when possible

## 8. Software and Data Integrity Failures

### Implementation
- **Input Validation**: Comprehensive validation on all inputs
- **Data Sanitization**: Proper handling of user data

### Code Examples
```java
// Entity validation
@NotBlank(message = "Name is required")
@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
private String name;

@Email(message = "Email should be valid")
@NotBlank(message = "Email is required")
private String email;
```

### Best Practices
- Validate all inputs
- Sanitize data before processing
- Use signed dependencies

## 9. Security Logging and Monitoring Failures

### Implementation
- **Comprehensive Logging**: Detailed request/response logging
- **Sensitive Data Masking**: Passwords and tokens not logged
- **Security Event Logging**: Authentication and authorization events

### Code Examples
```java
// HttpBodyLoggingFilter.java
private boolean isSensitiveEndpoint(String uri) {
    return uri.contains("/auth/") || uri.contains("/login") || 
           uri.contains("/register") || uri.contains("/password");
}

// CustomAuthenticationProvider.java
logger.info("                    - Input password: [PROTECTED]");
logger.info("                    - Stored hash: [PROTECTED]");
```

### Best Practices
- Log security-relevant events
- Mask sensitive data in logs
- Monitor logs for suspicious activity

## 10. Server-Side Request Forgery (SSRF)

### Implementation
- **URL Validation**: Comprehensive URL validation with SSRF pattern detection
- **Network Restrictions**: Configurable allowed hosts and schemes
- **Protected HTTP Service**: Safe HTTP client for external requests
- **Request Filtering**: SSRF protection filter for incoming requests

### Code Examples
```java
// SsrfProtectionFilter.java
private static final List<Pattern> SSRF_PATTERNS = Arrays.asList(
    // Private IP ranges
    Pattern.compile("^(10\\.|172\\.(1[6-9]|2[0-9]|3[01])\\.|192\\.168\\.)"),
    // Loopback addresses
    Pattern.compile("^(127\\.|0\\.0\\.0|::1)"),
    // AWS metadata service
    Pattern.compile("^169\\.254\\.169\\.254"),
    // Docker internal
    Pattern.compile("^host\\.docker\\.internal")
);

// SsrfProtectedHttpService.java
public String get(String url) throws IOException, IllegalArgumentException {
    validateUrl(url);
    // Safe HTTP request implementation
}
```

### Configuration
```yaml
security:
  ssrf:
    allowed-hosts: ${SSRF_ALLOWED_HOSTS:localhost,127.0.0.1,::1}
    allowed-schemes: ${SSRF_ALLOWED_SCHEMES:http,https}
```

### Best Practices
- Validate all URLs before making external requests
- Use allowlists for external services
- Implement network segmentation
- Block access to internal services and metadata endpoints

## CSRF Protection Status

### Implementation Status: DISABLED (Acceptable for REST APIs)
- **Reason**: This is a stateless REST API using JWT tokens
- **Protection**: JWT tokens provide sufficient protection against CSRF attacks
- **Documentation**: Added comments explaining the security implications

### Code Example
```java
// SecurityConfig.java
// CSRF is disabled for REST API with JWT authentication
// JWT tokens provide sufficient protection against CSRF attacks
// For web applications with session-based auth, enable CSRF protection
.csrf(AbstractHttpConfigurer::disable)
```

### When to Enable CSRF Protection
- **Enable**: Web applications with session-based authentication
- **Disable**: Stateless REST APIs with token-based authentication (JWT)
- **Consider**: Hybrid applications may need selective CSRF protection

## Additional Security Features

### Rate Limiting
```java
// RateLimitFilter.java
@Value("${security.rate-limit.requests-per-minute:10}")
private int requestsPerMinute;

private Bucket resolveBucket(String key) {
    return buckets.computeIfAbsent(key, k -> Bucket4j.builder()
        .addLimit(Bandwidth.classic(requestsPerMinute, Refill.greedy(requestsPerMinute, Duration.ofMinutes(1))))
        .build());
}
```

### Configuration Management
```yaml
# Environment-based configuration
security:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:https://yourdomain.com}
  rate-limit:
    requests-per-minute: ${SECURITY_RATE_LIMIT_REQUESTS_PER_MINUTE:10}
  account-lockout:
    max-attempts: ${SECURITY_ACCOUNT_LOCKOUT_MAX_ATTEMPTS:5}
    lock-duration-minutes: ${SECURITY_ACCOUNT_LOCKOUT_LOCK_DURATION_MINUTES:15}
  password:
    min-length: ${SECURITY_PASSWORD_MIN_LENGTH:8}
```

## Testing Security Features

### Account Lockout Test
```bash
# Test account lockout after 5 failed attempts
for i in {1..6}; do
  curl -X POST http://localhost:8085/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"john@example.com","password":"wrongpassword"}'
done
```

### Rate Limiting Test
```bash
# Test rate limiting
for i in {1..15}; do
  curl http://localhost:8085/api/test/rate-limit
done
```

### Password Policy Test
```bash
# Test strong password requirement
curl -X POST http://localhost:8085/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"weak"}'
```

## Security Checklist

- [x] Implement role-based access control
- [x] Use strong password hashing (BCrypt)
- [x] Implement JWT with secure signing
- [x] Add input validation and sanitization
- [x] Configure secure HTTP headers
- [x] Restrict CORS origins
- [x] Implement account lockout
- [x] Add rate limiting
- [x] Mask sensitive data in logs
- [x] Use parameterized queries
- [x] Configure environment-based secrets
- [x] Protect development tools
- [x] Add comprehensive logging
- [x] Implement strong password policy

## Monitoring and Maintenance

### Regular Tasks
1. **Dependency Updates**: Monthly dependency security updates
2. **Log Monitoring**: Daily security log review
3. **Configuration Audits**: Quarterly security configuration review
4. **Penetration Testing**: Annual security assessments
5. **Secret Rotation**: Quarterly JWT secret rotation

### Security Metrics
- Failed login attempts
- Account lockouts
- Rate limit violations
- Authentication failures
- Suspicious activity patterns

## Conclusion

This implementation provides comprehensive protection against OWASP Top 10 threats. Regular monitoring, updates, and security assessments are essential to maintain the security posture of the application.

For additional security measures, consider:
- Multi-factor authentication
- API rate limiting per user
- Advanced threat detection
- Security information and event management (SIEM)
- Regular penetration testing 