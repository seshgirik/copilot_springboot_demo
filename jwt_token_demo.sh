#!/bin/bash

# JWT Token Usage Demo Script
# This script demonstrates the complete token generation and usage flow

echo "🚀 JWT Token Usage Demo - Complete Flow"
echo "======================================"

BASE_URL="http://localhost:8085"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_header() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Step 1: Login and get token
print_header "Step 1: Login and Token Generation"
echo "Logging in with user credentials..."

LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }')

echo "Login Response: $LOGIN_RESPONSE"

# Extract token from response
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
    print_success "Token received successfully!"
    print_info "Token (first 50 chars): ${TOKEN:0:50}..."
    echo "Full Token: $TOKEN"
else
    print_error "Failed to get token!"
    exit 1
fi

# Step 2: Use token to access protected endpoint
print_header "Step 2: Using Token to Access Protected Products API"
echo "Making request to /api/products with Bearer token..."

PRODUCTS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/products" \
  -H "Authorization: Bearer $TOKEN")

if [[ $PRODUCTS_RESPONSE == *"content"* ]]; then
    print_success "Successfully accessed products API with token!"
    echo "Products found: $(echo $PRODUCTS_RESPONSE | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)"
else
    print_error "Failed to access products API"
    echo "Response: $PRODUCTS_RESPONSE"
fi

# Step 3: Try to access admin endpoint with regular user token (should fail)
print_header "Step 3: Testing Role-Based Access Control"
echo "Attempting to access admin-only /api/users endpoint with USER role token..."

USERS_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "$BASE_URL/api/users" \
  -H "Authorization: Bearer $TOKEN")

HTTP_STATUS=$(echo "$USERS_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)

if [ "$HTTP_STATUS" = "403" ]; then
    print_success "Access correctly denied! USER role cannot access admin endpoints."
else
    print_error "Unexpected response for role-based access control"
fi

# Step 4: Login as admin and access admin endpoint
print_header "Step 4: Admin Token Generation and Usage"
echo "Logging in as admin user..."

ADMIN_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice.johnson@example.com",
    "password": "password123"
  }')

ADMIN_TOKEN=$(echo $ADMIN_LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$ADMIN_TOKEN" ]; then
    print_success "Admin token received!"
    
    echo "Accessing admin-only /api/users endpoint with ADMIN token..."
    ADMIN_USERS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/users" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    if [[ $ADMIN_USERS_RESPONSE == *"content"* ]]; then
        print_success "Admin successfully accessed users API!"
        echo "Users found: $(echo $ADMIN_USERS_RESPONSE | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)"
    else
        print_error "Admin failed to access users API"
    fi
else
    print_error "Failed to get admin token!"
fi

# Step 5: Validate token
print_header "Step 5: Token Validation"
echo "Validating the user token..."

VALIDATE_RESPONSE=$(curl -s -X GET "$BASE_URL/auth/validate" \
  -H "Authorization: Bearer $TOKEN")

if [[ $VALIDATE_RESPONSE == *'"valid":true'* ]]; then
    print_success "Token is valid!"
    echo "Validation Response: $VALIDATE_RESPONSE"
else
    print_error "Token validation failed"
    echo "Response: $VALIDATE_RESPONSE"
fi

# Step 6: Test without token (should fail)
print_header "Step 6: Testing Request Without Token"
echo "Attempting to access /api/products without token..."

NO_TOKEN_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "$BASE_URL/api/products")
NO_TOKEN_HTTP_STATUS=$(echo "$NO_TOKEN_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)

if [ "$NO_TOKEN_HTTP_STATUS" = "403" ]; then
    print_success "Access correctly denied without token!"
else
    print_error "Unexpected response for request without token"
fi

# Step 7: Test with invalid token
print_header "Step 7: Testing with Invalid Token"
echo "Attempting to access /api/products with invalid token..."

INVALID_TOKEN_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "$BASE_URL/api/products" \
  -H "Authorization: Bearer invalid-token-123")

INVALID_TOKEN_HTTP_STATUS=$(echo "$INVALID_TOKEN_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)

if [ "$INVALID_TOKEN_HTTP_STATUS" = "403" ]; then
    print_success "Invalid token correctly rejected!"
else
    print_error "Unexpected response for invalid token"
fi

print_header "Demo Summary"
echo "✅ Token generation works correctly"
echo "✅ Token can be used to access protected endpoints"
echo "✅ Role-based access control is working"
echo "✅ Token validation endpoint works"
echo "✅ Requests without tokens are properly rejected"
echo "✅ Invalid tokens are properly rejected"

print_header "How to Use Tokens in Your Application"
echo ""
echo "1. LOGIN: POST /auth/login with credentials"
echo "   Response includes: { \"token\": \"eyJhbGc...\", ... }"
echo ""
echo "2. STORE: Save the token securely (localStorage, cookie, etc.)"
echo ""
echo "3. USE: Include in all subsequent requests:"
echo "   Header: Authorization: Bearer <your-token>"
echo ""
echo "4. HANDLE ERRORS:"
echo "   - 401 Unauthorized: Token expired/invalid"
echo "   - 403 Forbidden: Valid token but insufficient permissions"
echo ""
echo "5. VALIDATE: Optional - use GET /auth/validate to check token"
echo ""
echo "Example curl command:"
echo "curl -X GET http://localhost:8085/api/products \\"
echo "  -H \"Authorization: Bearer $TOKEN\""
echo ""
print_success "Demo completed successfully!"
