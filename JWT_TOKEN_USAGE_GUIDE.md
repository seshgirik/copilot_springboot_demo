# JWT Token Usage Guide

This guide demonstrates how to use JWT tokens in subsequent requests after authentication.

## Token Generation Flow

When a user logs in, the application follows this detailed flow:

### 1. Login Request
```bash
curl -X POST http://localhost:8085/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

### 2. Login Response with Token
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiVVNFUiIsInN1YiI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwiaWF0IjoxNzIxNjY4MDUyLCJleHAiOjE3MjE3NTQ0NTJ9.abc123...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "role": "USER"
  }
}
```

## How to Use the Token in Subsequent Requests

### Authorization Header Format
All protected endpoints require the JWT token in the `Authorization` header using the **Bearer** scheme:

```
Authorization: Bearer <your-jwt-token>
```

### Examples of Using the Token

#### 1. Access Products API (Protected Endpoint)
```bash
curl -X GET http://localhost:8085/api/products \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiVVNFUiIsInN1YiI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwiaWF0IjoxNzIxNjY4MDUyLCJleHAiOjE3MjE3NTQ0NTJ9.abc123..."
```

#### 2. Create a New Product (POST with Token)
```bash
curl -X POST http://localhost:8085/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "name": "New Product",
    "description": "Product description",
    "price": 29.99,
    "categoryId": 1
  }'
```

#### 3. Update a Product (PUT with Token)
```bash
curl -X PUT http://localhost:8085/api/products/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "name": "Updated Product",
    "description": "Updated description",
    "price": 39.99,
    "categoryId": 1
  }'
```

#### 4. Delete a Product (DELETE with Token)
```bash
curl -X DELETE http://localhost:8085/api/products/1 \
  -H "Authorization: Bearer <your-jwt-token>"
```

#### 5. Access Admin-Only Endpoints (Requires ADMIN role)
```bash
curl -X GET http://localhost:8085/api/users \
  -H "Authorization: Bearer <admin-user-jwt-token>"
```

## Token Validation Process

When you make a request with a token, the application automatically:

1. **Extracts the token** from the `Authorization` header
2. **Validates the token signature** using the secret key
3. **Checks token expiration** (default: 24 hours)
4. **Extracts user information** (username, role) from token claims
5. **Loads user details** from the database
6. **Sets authentication context** for the request
7. **Authorizes access** based on user role and endpoint requirements

## Frontend Integration Examples

### JavaScript/AJAX
```javascript
// Store token after login
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
localStorage.setItem('jwtToken', token);

// Use token in subsequent requests
const token = localStorage.getItem('jwtToken');
const productsResponse = await fetch('/api/products', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const products = await productsResponse.json();
```

### React Example
```jsx
import { useState, useEffect } from 'react';

function App() {
  const [token, setToken] = useState(localStorage.getItem('jwtToken'));
  const [products, setProducts] = useState([]);

  const login = async (email, password) => {
    const response = await fetch('/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    
    const { token } = await response.json();
    setToken(token);
    localStorage.setItem('jwtToken', token);
  };

  const fetchProducts = async () => {
    if (!token) return;
    
    const response = await fetch('/api/products', {
      headers: { 
        'Authorization': `Bearer ${token}` 
      }
    });
    
    if (response.ok) {
      const products = await response.json();
      setProducts(products);
    } else if (response.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('jwtToken');
      setToken(null);
    }
  };

  useEffect(() => {
    if (token) {
      fetchProducts();
    }
  }, [token]);

  return (
    <div>
      {token ? (
        <ProductList products={products} />
      ) : (
        <LoginForm onLogin={login} />
      )}
    </div>
  );
}
```

### Axios Interceptor (Global Token Handling)
```javascript
import axios from 'axios';

// Create axios instance
const api = axios.create({
  baseURL: 'http://localhost:8085'
});

// Request interceptor to add token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwtToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle token expiration
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('jwtToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Usage
const fetchProducts = () => api.get('/api/products');
const createProduct = (product) => api.post('/api/products', product);
```

## Token Validation Endpoint

You can also validate if a token is still valid:

```bash
curl -X GET http://localhost:8085/auth/validate \
  -H "Authorization: Bearer <your-jwt-token>"
```

**Response for valid token:**
```json
{
  "valid": true,
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "role": "USER"
  }
}
```

**Response for invalid token:**
```json
{
  "valid": false
}
```

## Error Handling

### Common HTTP Status Codes

- **200 OK**: Request successful with valid token
- **401 Unauthorized**: Missing, invalid, or expired token
- **403 Forbidden**: Valid token but insufficient permissions (wrong role)

### Example Error Responses

#### Missing Token
```json
{
  "error": "Access Denied"
}
```

#### Invalid/Expired Token
```json
{
  "error": "Unauthorized"
}
```

## Best Practices

### 1. Store Tokens Securely
- **Frontend**: Use secure HTTP-only cookies or secure storage
- **Mobile**: Use secure keychain/keystore
- **Never** store tokens in plain localStorage for production apps

### 2. Handle Token Expiration
```javascript
const makeRequest = async (url, options = {}) => {
  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Authorization': `Bearer ${token}`,
        ...options.headers
      }
    });

    if (response.status === 401) {
      // Token expired, redirect to login
      redirectToLogin();
      return;
    }

    return response;
  } catch (error) {
    console.error('Request failed:', error);
  }
};
```

### 3. Refresh Token Strategy
```javascript
// Check token expiration before requests
const isTokenExpired = (token) => {
  const payload = JSON.parse(atob(token.split('.')[1]));
  return payload.exp * 1000 < Date.now();
};

// Automatic login renewal
if (isTokenExpired(token)) {
  await refreshToken(); // Implement token refresh logic
}
```

### 4. Logout Implementation
```javascript
const logout = () => {
  localStorage.removeItem('jwtToken');
  // Clear other user data
  window.location.href = '/login';
};
```

## Testing with Different User Roles

### Regular User (USER role)
```bash
# Login as regular user
curl -X POST http://localhost:8085/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'

# Can access products
curl -X GET http://localhost:8085/api/products \
  -H "Authorization: Bearer <user-token>"

# Cannot access admin endpoints (will get 403)
curl -X GET http://localhost:8085/api/users \
  -H "Authorization: Bearer <user-token>"
```

### Admin User (ADMIN role)
```bash
# Login as admin
curl -X POST http://localhost:8085/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice.johnson@example.com",
    "password": "password123"
  }'

# Can access both products and users
curl -X GET http://localhost:8085/api/products \
  -H "Authorization: Bearer <admin-token>"

curl -X GET http://localhost:8085/api/users \
  -H "Authorization: Bearer <admin-token>"
```

## Summary

1. **Get Token**: Login to receive JWT token
2. **Include Token**: Add `Authorization: Bearer <token>` header to all requests
3. **Handle Errors**: Check for 401/403 responses and handle accordingly
4. **Token Expiration**: Tokens expire after 24 hours by default
5. **Role-Based Access**: Different endpoints require different user roles
6. **Validate Token**: Use `/auth/validate` to check token validity

The JWT token contains all necessary user information (username, role) and is automatically validated by the application for each protected request.
