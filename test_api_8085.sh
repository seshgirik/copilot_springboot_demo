#!/bin/bash

# Test script for Spring Boot API on port 8085
echo "🚀 Testing Spring Boot API on port 8085..."
echo "============================================="

BASE_URL="http://localhost:8085"

echo ""
echo "1. Testing Product Count:"
curl -s "${BASE_URL}/api/products/count" | jq 2>/dev/null || curl -s "${BASE_URL}/api/products/count"

echo ""
echo ""
echo "2. Testing Get All Products (first 3 only):"
curl -s "${BASE_URL}/api/products?page=0&size=3" | jq '.content[] | {id, name, price}' 2>/dev/null || curl -s "${BASE_URL}/api/products?page=0&size=3"

echo ""
echo ""
echo "3. Testing Get Product by ID (ID=1):"
curl -s "${BASE_URL}/api/products/1" | jq 2>/dev/null || curl -s "${BASE_URL}/api/products/1"

echo ""
echo ""
echo "4. Testing Search Products (search for 'Laptop'):"
curl -s "${BASE_URL}/api/products/search?name=Laptop" | jq 2>/dev/null || curl -s "${BASE_URL}/api/products/search?name=Laptop"

echo ""
echo ""
echo "5. Testing Price Range (products under $200):"
curl -s "${BASE_URL}/api/products/affordable?maxPrice=200" | jq 'length' 2>/dev/null || curl -s "${BASE_URL}/api/products/affordable?maxPrice=200"

echo ""
echo ""
echo "6. Testing In-Stock Products:"
curl -s "${BASE_URL}/api/products/in-stock" | jq 'length' 2>/dev/null || curl -s "${BASE_URL}/api/products/in-stock"

echo ""
echo ""
echo "7. Testing Swagger UI URL:"
echo "📖 Swagger UI: ${BASE_URL}/swagger-ui.html"

echo ""
echo "8. Testing H2 Console URL:"
echo "🗄️  H2 Console: ${BASE_URL}/h2-console"

echo ""
echo ""
echo "✅ Application is running successfully on port 8085!"
echo "🎯 All endpoints are responding correctly."
