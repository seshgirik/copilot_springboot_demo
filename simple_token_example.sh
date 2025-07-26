#!/bin/bash

# Simple Token Usage Example
# Shows how to login, extract token, and use it for subsequent requests

echo "=== Simple JWT Token Usage Example ==="

# Step 1: Login and extract token
echo "1. Logging in and extracting token..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8085/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }')

# Extract just the token value
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "Token extracted: ${TOKEN:0:50}..."

# Step 2: Save token to file for reuse
echo $TOKEN > token.txt
echo "2. Token saved to token.txt"

# Step 3: Read token from file and use it
echo "3. Reading token from file and using it..."
SAVED_TOKEN=$(cat token.txt)

# Make request using saved token
RESPONSE=$(curl -s -X GET http://localhost:8085/api/products \
  -H "Authorization: Bearer $SAVED_TOKEN")

echo "4. API Response: Successfully fetched $(echo $RESPONSE | grep -o '"totalElements":[0-9]*' | cut -d':' -f2) products"

echo "5. Cleanup..."
rm token.txt

echo ""
echo "✅ Complete! This shows how to:"
echo "   - Login and extract the JWT token"
echo "   - Save the token for later use"
echo "   - Use the token in subsequent API requests"
echo ""
echo "In a real application, you would:"
echo "   - Store tokens securely (not in plain text files)"
echo "   - Handle token expiration (24 hours by default)"
echo "   - Include error handling for invalid tokens"
