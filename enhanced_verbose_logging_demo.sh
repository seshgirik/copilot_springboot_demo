#!/bin/bash

# Enhanced Comprehensive Verbose Logging Demonstration Script
# This script demonstrates verbose logging for both REST and gRPC APIs with all CRUD operations

echo "🚀 === Enhanced Comprehensive Verbose Logging Demonstration ==="
echo "📋 This script will demonstrate verbose logging for:"
echo "   • REST API endpoints (HTTP/JSON)"
echo "   • gRPC service calls (HTTP/2/Protobuf)"
echo "   • All CRUD operations with request/response logging"
echo "   • Service layer business logic logging"
echo "   • Database operation logging"
echo ""
echo "🎯 The application logs every:"
echo "   • HTTP request/response headers and bodies"
echo "   • gRPC method calls and responses"
echo "   • Service method invocations with parameters"
echo "   • Database queries and results"
echo "   • Error handling and validation"
echo ""

# Wait for application to be ready
echo "⏳ Waiting for application to start..."
sleep 15

# Check if application is running
echo "🔍 Checking if Spring Boot application is running..."
if ! curl -s http://localhost:8085/api/products/count > /dev/null; then
    echo "❌ Application is not ready. Please start with: mvn spring-boot:run"
    exit 1
fi

if ! grpcurl -plaintext localhost:9090 grpc.health.v1.Health/Check > /dev/null 2>&1; then
    echo "❌ gRPC server is not ready. Please ensure gRPC is properly configured."
    exit 1
fi

echo "✅ Both REST (port 8085) and gRPC (port 9090) servers are ready!"
echo ""

# Start monitoring logs
echo "📊 === VERBOSE LOGGING DEMONSTRATION ==="
echo "🔍 Open application logs to see detailed verbose logging for each operation below:"
echo "   tail -f nohup.out"
echo ""

# Demonstration 1: REST API CRUD Operations
echo "🌐 === REST API CRUD Operations with Verbose Logging ==="
echo ""

echo "1️⃣ Creating a new product via REST API..."
echo "   📡 POST /api/products"
curl -s -X POST http://localhost:8085/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Verbose Logging Test Product",
    "description": "A product created to demonstrate verbose logging",
    "price": 99.99,
    "quantity": 50
  }' | jq '.'
echo ""

echo "2️⃣ Getting all products via REST API..."
echo "   📡 GET /api/products"
curl -s "http://localhost:8085/api/products?page=0&size=5" | jq '.content | length'
echo ""

echo "3️⃣ Searching products by name via REST API..."
echo "   📡 GET /api/products/search?name=test"
curl -s "http://localhost:8085/api/products/search?name=test" | jq 'length'
echo ""

echo "4️⃣ Getting product by ID via REST API..."
echo "   📡 GET /api/products/1"
curl -s http://localhost:8085/api/products/1 | jq '.name'
echo ""

echo "5️⃣ Updating product stock via REST API..."
echo "   📡 PATCH /api/products/1/stock?quantity=75"
curl -s -X PATCH "http://localhost:8085/api/products/1/stock?quantity=75" | jq '.quantity'
echo ""

echo "6️⃣ Updating product via REST API..."
echo "   📡 PUT /api/products/1"
curl -s -X PUT http://localhost:8085/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Verbose Test Product",
    "description": "Updated description for verbose logging demo",
    "price": 149.99,
    "quantity": 100
  }' | jq '.name'
echo ""

# Wait between REST and gRPC demonstrations
echo "⏸️ Waiting 3 seconds before gRPC demonstration..."
sleep 3
echo ""

# Demonstration 2: gRPC Operations
echo "🔌 === gRPC CRUD Operations with Verbose Logging ==="
echo ""

echo "7️⃣ Creating a new user via gRPC..."
echo "   📡 gRPC CreateUser"
grpcurl -plaintext -d '{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+1-555-0123"
}' localhost:9090 userservice.UserService/CreateUser | jq '.user.name'
echo ""

echo "8️⃣ Getting user by ID via gRPC..."
echo "   📡 gRPC GetUser"
grpcurl -plaintext -d '{"id": 1}' localhost:9090 userservice.UserService/GetUser | jq '.user.email'
echo ""

echo "9️⃣ Getting all users via gRPC..."
echo "   📡 gRPC GetAllUsers"
grpcurl -plaintext -d '{
  "page": 0,
  "size": 10
}' localhost:9090 userservice.UserService/GetAllUsers | jq '.users | length'
echo ""

echo "🔟 Creating a new product via gRPC..."
echo "   📡 gRPC CreateProduct"
grpcurl -plaintext -d '{
  "name": "gRPC Test Product",
  "description": "A product created via gRPC for verbose logging demo",
  "price": 199.99,
  "quantity": 25
}' localhost:9090 productservice.ProductService/CreateProduct | jq '.product.name'
echo ""

echo "1️⃣1️⃣ Getting product by ID via gRPC..."
echo "   📡 gRPC GetProduct"
grpcurl -plaintext -d '{"id": 2}' localhost:9090 productservice.ProductService/GetProduct | jq '.product.price'
echo ""

echo "1️⃣2️⃣ Getting all products via gRPC..."
echo "   📡 gRPC GetAllProducts"
grpcurl -plaintext -d '{
  "page": 0,
  "size": 5
}' localhost:9090 productservice.ProductService/GetAllProducts | jq '.products | length'
echo ""

echo "1️⃣3️⃣ Updating product via gRPC..."
echo "   📡 gRPC UpdateProduct"
grpcurl -plaintext -d '{
  "id": 2,
  "name": "Updated gRPC Product",
  "description": "Updated via gRPC for logging demonstration",
  "price": 299.99,
  "quantity": 50
}' localhost:9090 productservice.ProductService/UpdateProduct | jq '.product.name'
echo ""

echo "1️⃣4️⃣ Deleting product via gRPC..."
echo "   📡 gRPC DeleteProduct"
grpcurl -plaintext -d '{"id": 2}' localhost:9090 productservice.ProductService/DeleteProduct | jq '.success'
echo ""

# Final verification
echo ""
echo "1️⃣5️⃣ Final verification - Getting product count..."
echo "   📡 GET /api/products/count"
curl -s http://localhost:8085/api/products/count
echo ""

echo ""
echo "🎉 === Verbose Logging Demonstration Complete! ==="
echo ""
echo "📊 What was logged during this demonstration:"
echo "   ✅ HTTP request/response headers and bodies (via HttpLoggingInterceptor & HttpBodyLoggingFilter)"
echo "   ✅ gRPC method calls and message payloads (via GrpcLoggingInterceptor)"
echo "   ✅ Service layer method calls with parameters and results"
echo "   ✅ Database queries and execution details"
echo "   ✅ Business logic flow and decision points"
echo "   ✅ Error handling and validation messages"
echo ""
echo "🔍 To see all the verbose logs, check:"
echo "   tail -f nohup.out"
echo "   OR"
echo "   less nohup.out"
echo ""
echo "🏗️ Architecture: Single Spring Boot application with dual protocols:"
echo "   • REST API (HTTP/JSON) on port 8085"
echo "   • gRPC API (HTTP/2/Protobuf) on port 9090"
echo "   • Shared business logic in service layer"
echo "   • Comprehensive logging at all layers"
echo ""
echo "📖 For more information, see:"
echo "   • ARCHITECTURE_DIAGRAM.md"
echo "   • REQUEST_FLOW_DIAGRAM.md"
echo "   • MESSAGE_MONITORING_GUIDE.md"
echo ""

