# 🧪 Product API Test Commands

## Prerequisites
1. Start the application: `mvn spring-boot:run`
2. Wait for startup message: "Tomcat started on port(s): 8085"
3. Run the curl commands below

**🆕 gRPC Server**: The application also runs a gRPC server on port **9090**
- See `GRPC_TESTING_GUIDE.md` for gRPC testing instructions
- Use `./test_grpc_messages.sh` for quick gRPC status check

---

## 📋 Basic CRUD Operations

### 1️⃣ GET ALL PRODUCTS (with pagination)
```bash
# Default pagination (page=0, size=10)
curl -X GET http://localhost:8085/api/products

# Custom pagination
curl -X GET "http://localhost:8085/api/products?page=0&size=5"
```

### 2️⃣ GET PRODUCT COUNT
```bash
curl -X GET http://localhost:8085/api/products/count
```products/count
```

### 3️⃣ CREATE A NEW PRODUCT
```bash
curl -X POST http://localhost:8085/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Laptop",
    "description": "High-performance laptop for testing",
    "price": 999.99,
    "quantity": 10
  }'
```

### 4️⃣ GET PRODUCT BY ID
```bash
# Replace {id} with actual product ID (e.g., 1, 2, 3...)
curl -X GET http://localhost:8085/api/products/1
```

### 5️⃣ UPDATE PRODUCT (Full Update)
```bash
curl -X PUT http://localhost:8085/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Laptop",
    "description": "Updated high-performance laptop",
    "price": 1199.99,
    "quantity": 8
  }'
```

### 6️⃣ UPDATE PRODUCT STOCK (Partial Update)
```bash
curl -X PATCH "http://localhost:8085/api/products/1/stock?quantity=15"
```

### 7️⃣ DELETE PRODUCT
```bash
curl -X DELETE http://localhost:8085/api/products/1
```

---

## 🔍 Search and Filter Operations

### 8️⃣ SEARCH PRODUCTS BY NAME
```bash
curl -X GET "http://localhost:8085/api/products/search?name=laptop"
```

### 9️⃣ GET PRODUCTS BY PRICE RANGE
```bash
curl -X GET "http://localhost:8085/api/products/price-range?minPrice=100&maxPrice=1000"
```

### 🔟 GET IN-STOCK PRODUCTS
```bash
curl -X GET http://localhost:8085/api/products/in-stock
```

### 1️⃣1️⃣ GET AFFORDABLE PRODUCTS
```bash
curl -X GET "http://localhost:8085/api/products/affordable?maxPrice=800"
```

---

## 🧪 Advanced Test Examples

### Create Multiple Products for Testing
```bash
# Create Product 1
curl -X POST http://localhost:8085/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High-end gaming laptop with RTX graphics",
    "price": 1599.99,
    "quantity": 5
  }'

# Create Product 2  
curl -X POST http://localhost:8085/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Office Keyboard",
    "description": "Ergonomic keyboard for office use",
    "price": 79.99,
    "quantity": 25
  }'

# Create Product 3 (out of stock)
curl -X POST http://localhost:8085/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Mouse",
    "description": "Bluetooth wireless mouse",
    "price": 29.99,
    "quantity": 0
  }'
```

### Test Error Scenarios
```bash
# Test 404 - Non-existent product
curl -X GET http://localhost:8085/api/products/9999

# Test validation error - Invalid product data
curl -X POST http://localhost:8085/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "description": "Invalid product with empty name",
    "price": -10.00,
    "quantity": -5
  }'
```

---

## 🧑‍💼 USER MANAGEMENT API

### 1️⃣ CREATE NEW USERS

#### Basic User Creation
```bash
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "123-456-7890"
  }'
```

#### Multiple User Examples
```bash
# Create User 1
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "email": "alice.johnson@example.com",
    "phone": "555-0123"
  }'

# Create User 2
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bob Smith",
    "email": "bob.smith@example.com",
    "phone": "555-0456"
  }'

# Create User 3
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Carol Williams",
    "email": "carol.williams@example.com",
    "phone": "555-0789"
  }'
```

### 2️⃣ GET ALL USERS
```bash
# Default pagination (page=0, size=10)
curl -X GET http://localhost:8085/api/users

# Custom pagination
curl -X GET "http://localhost:8085/api/users?page=0&size=5"
```

### 3️⃣ GET USER BY ID
```bash
# Replace {id} with actual user ID (e.g., 1, 2, 3...)
curl -X GET http://localhost:8085/api/users/1
```

### 4️⃣ SEARCH USERS BY NAME
```bash
# Search for users with name containing "john"
curl -X GET "http://localhost:8085/api/users/search/name?name=john"
```

### 5️⃣ SEARCH USERS BY EMAIL
```bash
# Search for users with email containing "example.com"
curl -X GET "http://localhost:8085/api/users/search/email?email=example.com"
```

### 6️⃣ UPDATE USER (Full Update)
```bash
curl -X PUT http://localhost:8085/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Updated",
    "email": "john.updated@example.com",
    "phone": "999-888-7777"
  }'
```

### 7️⃣ DELETE USER
```bash
curl -X DELETE http://localhost:8085/api/users/1
```

### 8️⃣ GET USER COUNT
```bash
curl -X GET http://localhost:8085/api/users/count
```

---

## 🧪 **Validation Testing**

### ❌ Invalid User Creation (Missing Required Fields)
```bash
# This should return 400 Bad Request
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "123-456-7890"
  }'
```

### ❌ Invalid Email Format
```bash
# This should return 400 Bad Request
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Invalid User",
    "email": "invalid-email",
    "phone": "123-456-7890"
  }'
```

### ❌ Duplicate Email
```bash
# First create a user
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "First User",
    "email": "duplicate@example.com",
    "phone": "123-456-7890"
  }'

# Then try to create another with same email (should return 409 Conflict)
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Second User",
    "email": "duplicate@example.com",
    "phone": "987-654-3210"
  }'
```

---

## 🚀 **Quick Test Script for Users**

```bash
#!/bin/bash
echo "🧑‍💼 Testing User API..."

# Create test users
echo "Creating users..."
curl -s -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Test User 1", "email": "test1@example.com", "phone": "111-111-1111"}'

curl -s -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Test User 2", "email": "test2@example.com", "phone": "222-222-2222"}'

# Get all users
echo "Getting all users..."
curl -s http://localhost:8085/api/users | jq .

echo "✅ User API test completed!"
```
