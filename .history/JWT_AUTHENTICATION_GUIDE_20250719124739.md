# JWT Authentication Guide

This guide explains how to use JWT (JSON Web Token) authentication in the Spring Boot application.

## Overview

The application now supports JWT-based authentication with the following features:

- **User Registration**: Create new user accounts with email and password
- **User Login**: Authenticate users and receive JWT tokens
- **Token Validation**: Verify JWT tokens and extract user information
- **Role-based Access Control**: Different endpoints require different user roles
- **Protected Endpoints**: Secure API endpoints that require authentication

## Architecture

### Components

1. **JwtUtil**: Utility class for JWT token generation, validation, and claim extraction
2. **JwtAuthenticationFilter**: Filter that intercepts requests and validates JWT tokens
3. **SecurityConfig**: Spring Security configuration for authentication and authorization
4. **CustomUserDetailsService**: Service to load user details from the database
5. **AuthController**: REST controller for authentication endpoints

### Security Flow

1. User registers/logs in via `/auth/register` or `/auth/login`
2. Server validates credentials and returns a JWT token
3. Client includes the token in subsequent requests via `Authorization: Bearer <token>` header
4. `JwtAuthenticationFilter` intercepts requests and validates the token
5. If valid, the user is authenticated and can access protected endpoints

## API Endpoints

### Authentication Endpoints

#### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "password": "password123"
}
```

**Response:**
```json
{
  "message": "User registered successfully",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

#### Login User
```http
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

#### Validate Token
```http
GET /auth/validate
Authorization: Bearer <token>
```

**Response:**
```json
{
  "valid": true,
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

### Protected Endpoints

#### Products API (Requires USER or ADMIN role)
```http
GET /api/products
Authorization: Bearer <token>
```

#### Users API (Requires ADMIN role)
```http
GET /api/users
Authorization: Bearer <token>
```

## User Roles

- **USER**: Can access product endpoints
- **ADMIN**: Can access both product and user endpoints

## Configuration

### JWT Settings (application.yml)
```yaml
jwt:
  secret: your-secret-key-here-make-it-long-and-secure-in-production
  expiration: 86400000 # 24 hours in milliseconds
```

### Security Configuration
- **Public endpoints**: `/auth/**`, `/h2-console/**`, `/swagger-ui/**`, `/api-docs/**`
- **Product endpoints**: Require USER or ADMIN role
- **User endpoints**: Require ADMIN role
- **Session management**: Stateless (no server-side sessions)

## Testing

### Using the Test Script
```bash
./test_jwt_auth.sh
```

This script will:
1. Register a new user
2. Login with existing credentials
3. Test protected endpoint access
4. Test unauthorized access blocking
5. Validate JWT tokens
6. Test role-based access control
7. Test admin endpoint access
8. Test invalid credentials handling
9. Test invalid token handling

### Manual Testing with curl

#### Register a new user:
```bash
curl -X POST http://localhost:8085/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "phone": "1234567890",
    "password": "password123"
  }'
```

#### Login:
```bash
curl -X POST http://localhost:8085/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

#### Access protected endpoint:
```bash
curl -X GET http://localhost:8085/api/products \
  -H "Authorization: Bearer <your-jwt-token>"
```

## Pre-configured Users

The application comes with pre-configured users for testing:

| Email | Password | Role |
|-------|----------|------|
| john.doe@example.com | password123 | USER |
| jane.smith@example.com | password123 | USER |
| alice.johnson@example.com | password123 | ADMIN |
| bob.wilson@example.com | password123 | USER |
| charlie.brown@example.com | password123 | USER |

## Security Features

### Password Security
- Passwords are hashed using BCrypt
- Minimum password length: 6 characters
- Passwords are never stored in plain text

### Token Security
- JWT tokens are signed with HMAC-SHA256
- Tokens have expiration time (24 hours by default)
- Tokens contain user role information
- Invalid tokens are automatically rejected

### CORS Configuration
- Cross-origin requests are allowed for development
- All HTTP methods are permitted
- All headers are allowed

## Error Handling

### Common Error Responses

#### Invalid Credentials
```json
{
  "error": "Invalid email or password"
}
```

#### Unauthorized Access
```json
{
  "error": "Access Denied"
}
```

#### Invalid Token
```json
{
  "error": "Unauthorized"
}
```

## Best Practices

### For Production Use

1. **Change the JWT secret**: Use a strong, unique secret key
2. **Set appropriate token expiration**: Consider shorter expiration times for sensitive applications
3. **Use HTTPS**: Always use HTTPS in production
4. **Implement token refresh**: Consider implementing refresh tokens for better security
5. **Add rate limiting**: Implement rate limiting for authentication endpoints
6. **Log security events**: Log authentication attempts and failures
7. **Regular security audits**: Regularly review and update security configurations

### Token Management

1. **Store tokens securely**: Store tokens in secure, HTTP-only cookies or secure storage
2. **Don't store sensitive data**: Avoid storing sensitive information in JWT payload
3. **Implement token revocation**: Consider implementing a token blacklist for logout
4. **Monitor token usage**: Track token usage patterns for security analysis

## Troubleshooting

### Common Issues

1. **Token not working**: Check if the token is properly formatted and not expired
2. **Access denied**: Verify the user has the required role for the endpoint
3. **Login fails**: Ensure the email and password are correct
4. **Registration fails**: Check if the email is already registered

### Debug Mode

Enable debug logging for security-related issues:
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    com.demo.springboot: DEBUG
```

## Integration with Frontend

### JavaScript Example
```javascript
// Login
const loginResponse = await fetch('/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'password123'
  })
});

const { token } = await loginResponse.json();

// Use token for authenticated requests
const productsResponse = await fetch('/api/products', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

### React Example
```jsx
const [token, setToken] = useState(localStorage.getItem('token'));

const login = async (email, password) => {
  const response = await fetch('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  
  const { token } = await response.json();
  setToken(token);
  localStorage.setItem('token', token);
};

const fetchProducts = async () => {
  const response = await fetch('/api/products', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  return response.json();
};
``` 