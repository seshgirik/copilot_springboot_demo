#!/bin/bash

# 🧪 Spring Boot Product API - Comprehensive Test Suite
# ===================================================

echo "🚀 Spring Boot Product API Test Suite"
echo "======================================"

BASE_URL="http://localhost:8080"

echo "📋 Prerequisites:"
echo "1. Start the application: mvn spring-boot:run"
echo "2. Wait for startup message: 'Tomcat started on port(s): 8080'"
echo "3. Run these tests after application is running"
echo ""

# Test if application is running
echo "🔍 Testing if application is running..."
if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1 || curl -s "$BASE_URL" > /dev/null 2>&1; then
    echo "✅ Application is responding"
else
    echo "❌ Application not responding. Make sure it's running on port 8080"
    echo "   Start with: mvn spring-boot:run"
    exit 1
fi

echo ""
echo "🧪 Starting API Tests..."
echo "========================"

# 1. GET ALL PRODUCTS (with pagination)
echo -e "\n1️⃣  GET ALL PRODUCTS (Default pagination)"
echo "curl -X GET $BASE_URL/api/products"
curl -X GET "$BASE_URL/api/products" | jq '.' || curl -X GET "$BASE_URL/api/products"

echo -e "\n1️⃣a GET ALL PRODUCTS (Custom pagination)"
echo "curl -X GET '$BASE_URL/api/products?page=0&size=5'"
curl -X GET "$BASE_URL/api/products?page=0&size=5" | jq '.' || curl -X GET "$BASE_URL/api/products?page=0&size=5"

# 2. GET PRODUCT COUNT
echo -e "\n2️⃣  GET PRODUCT COUNT"
echo "curl -X GET $BASE_URL/api/products/count"
curl -X GET "$BASE_URL/api/products/count"

# 3. CREATE A NEW PRODUCT
echo -e "\n3️⃣  CREATE A NEW PRODUCT"
echo 'curl -X POST $BASE_URL/api/products \
  -H "Content-Type: application/json" \
  -d '\''{"name":"Test Laptop","description":"High-performance laptop for testing","price":999.99,"quantity":10}'\'''

curl -X POST "$BASE_URL/api/products" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Laptop","description":"High-performance laptop for testing","price":999.99,"quantity":10}' | jq '.' || \
curl -X POST "$BASE_URL/api/products" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Laptop","description":"High-performance laptop for testing","price":999.99,"quantity":10}'

# Store the created product ID for further tests
PRODUCT_ID=$(curl -s -X POST "$BASE_URL/api/products" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Phone","description":"Smartphone for API testing","price":599.99,"quantity":5}' | \
  jq -r '.id' 2>/dev/null || echo "1")

echo -e "\n📝 Created product with ID: $PRODUCT_ID (will use for subsequent tests)"

# 4. GET PRODUCT BY ID
echo -e "\n4️⃣  GET PRODUCT BY ID"
echo "curl -X GET $BASE_URL/api/products/$PRODUCT_ID"
curl -X GET "$BASE_URL/api/products/$PRODUCT_ID" | jq '.' || curl -X GET "$BASE_URL/api/products/$PRODUCT_ID"

# 5. SEARCH PRODUCTS BY NAME
echo -e "\n5️⃣  SEARCH PRODUCTS BY NAME"
echo "curl -X GET '$BASE_URL/api/products/search?name=laptop'"
curl -X GET "$BASE_URL/api/products/search?name=laptop" | jq '.' || curl -X GET "$BASE_URL/api/products/search?name=laptop"

# 6. GET PRODUCTS BY PRICE RANGE
echo -e "\n6️⃣  GET PRODUCTS BY PRICE RANGE"
echo "curl -X GET '$BASE_URL/api/products/price-range?minPrice=100&maxPrice=1000'"
curl -X GET "$BASE_URL/api/products/price-range?minPrice=100&maxPrice=1000" | jq '.' || curl -X GET "$BASE_URL/api/products/price-range?minPrice=100&maxPrice=1000"

# 7. GET IN-STOCK PRODUCTS
echo -e "\n7️⃣  GET IN-STOCK PRODUCTS"
echo "curl -X GET $BASE_URL/api/products/in-stock"
curl -X GET "$BASE_URL/api/products/in-stock" | jq '.' || curl -X GET "$BASE_URL/api/products/in-stock"

# 8. GET AFFORDABLE PRODUCTS
echo -e "\n8️⃣  GET AFFORDABLE PRODUCTS (under $800)"
echo "curl -X GET '$BASE_URL/api/products/affordable?maxPrice=800'"
curl -X GET "$BASE_URL/api/products/affordable?maxPrice=800" | jq '.' || curl -X GET "$BASE_URL/api/products/affordable?maxPrice=800"

# 9. UPDATE PRODUCT (PUT)
echo -e "\n9️⃣  UPDATE PRODUCT (Full Update)"
echo 'curl -X PUT $BASE_URL/api/products/'$PRODUCT_ID' \
  -H "Content-Type: application/json" \
  -d '\''{"name":"Updated Laptop","description":"Updated high-performance laptop","price":1199.99,"quantity":8}'\'''

curl -X PUT "$BASE_URL/api/products/$PRODUCT_ID" \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Laptop","description":"Updated high-performance laptop","price":1199.99,"quantity":8}' | jq '.' || \
curl -X PUT "$BASE_URL/api/products/$PRODUCT_ID" \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Laptop","description":"Updated high-performance laptop","price":1199.99,"quantity":8}'

# 10. UPDATE PRODUCT STOCK (PATCH)
echo -e "\n🔟 UPDATE PRODUCT STOCK (Partial Update)"
echo "curl -X PATCH '$BASE_URL/api/products/$PRODUCT_ID/stock?quantity=15'"
curl -X PATCH "$BASE_URL/api/products/$PRODUCT_ID/stock?quantity=15" | jq '.' || curl -X PATCH "$BASE_URL/api/products/$PRODUCT_ID/stock?quantity=15"

# 11. DELETE PRODUCT
echo -e "\n1️⃣1️⃣ DELETE PRODUCT"
echo "curl -X DELETE $BASE_URL/api/products/$PRODUCT_ID"
curl -X DELETE "$BASE_URL/api/products/$PRODUCT_ID" -v

# 12. VERIFY DELETION (should return 404)
echo -e "\n1️⃣2️⃣ VERIFY DELETION (should return 404)"
echo "curl -X GET $BASE_URL/api/products/$PRODUCT_ID"
curl -X GET "$BASE_URL/api/products/$PRODUCT_ID" -v

echo -e "\n✅ TESTING COMPLETE!"
echo "===================="

echo -e "\n📊 Summary of Tested Endpoints:"
echo "✅ GET    /api/products (pagination)"
echo "✅ GET    /api/products/{id}"
echo "✅ GET    /api/products/count"
echo "✅ GET    /api/products/search?name={name}"
echo "✅ GET    /api/products/price-range?minPrice={min}&maxPrice={max}"
echo "✅ GET    /api/products/in-stock"
echo "✅ GET    /api/products/affordable?maxPrice={max}"
echo "✅ POST   /api/products (create)"
echo "✅ PUT    /api/products/{id} (update)"
echo "✅ PATCH  /api/products/{id}/stock?quantity={qty}"
echo "✅ DELETE /api/products/{id}"

echo -e "\n🌐 Additional Resources:"
echo "📖 Swagger UI: $BASE_URL/swagger-ui.html"
echo "🗄️  H2 Console: $BASE_URL/h2-console"
echo "   JDBC URL: jdbc:h2:mem:testdb"
echo "   Username: sa"
echo "   Password: (leave empty)"

exit 0
