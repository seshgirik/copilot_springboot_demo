#!/bin/bash

# 🔍 Verbose Logging Demo for HTTP REST and gRPC

echo "🚀 Verbose Logging Demo"
echo "======================"

# Wait for application to start
echo "⏳ Waiting for application to start..."
sleep 20

echo ""
echo "📋 Testing HTTP REST API with Verbose Logging"
echo "=============================================="

# Terminal 1: Monitor HTTP logs
echo "🌐 Open another terminal and run this command to see HTTP logs:"
echo "   tail -f app.log | grep -E '(🌐|📥|📤|🔍|✅|❌)'"
echo ""

echo "▶️  Testing HTTP REST endpoints with detailed logging..."
echo ""

# Test 1: Get product count
echo "1️⃣ Testing GET /api/products/count"
echo "   Command: curl http://localhost:8085/api/products/count"
echo "   Expected logs: HTTP request/response + Service logs"
curl -s http://localhost:8085/api/products/count > /dev/null
echo "   ✅ Request sent - check logs!"
echo ""

# Test 2: Get all products  
echo "2️⃣ Testing GET /api/products"
echo "   Command: curl http://localhost:8085/api/products"
echo "   Expected logs: HTTP request/response + Pagination logs"
curl -s "http://localhost:8085/api/products?page=0&size=2" > /dev/null
echo "   ✅ Request sent - check logs!"
echo ""

# Test 3: Get product by ID
echo "3️⃣ Testing GET /api/products/1"
echo "   Command: curl http://localhost:8085/api/products/1"
echo "   Expected logs: HTTP request/response + Service lookup logs"
curl -s http://localhost:8085/api/products/1 > /dev/null
echo "   ✅ Request sent - check logs!"
echo ""

# Test 4: Create new product
echo "4️⃣ Testing POST /api/products (Create)"
echo "   Command: curl -X POST with JSON body"
echo "   Expected logs: HTTP request/response + JSON body + Service creation logs"
curl -s -X POST http://localhost:8085/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Laptop Verbose",
    "description": "Laptop with verbose logging demo",
    "price": 1299.99,
    "quantity": 5
  }' > /dev/null
echo "   ✅ Request sent - check logs!"
echo ""

echo "📋 Testing gRPC API with Verbose Logging"
echo "========================================"

echo "🚀 Open another terminal and run this command to see gRPC logs:"
echo "   tail -f app.log | grep -E '(🚀|📥|📤|✅|❌)'"
echo ""

echo "▶️  Testing gRPC endpoints with detailed logging..."
echo ""

# Test 1: gRPC Health Check
echo "5️⃣ Testing gRPC Health Check"
echo "   Command: grpcurl -plaintext localhost:9090 grpc.health.v1.Health/Check"
echo "   Expected logs: gRPC request/response + Method call logs"
grpcurl -plaintext localhost:9090 grpc.health.v1.Health/Check > /dev/null 2>&1
echo "   ✅ Request sent - check logs!"
echo ""

# Test 2: List gRPC services
echo "6️⃣ Testing gRPC Service Discovery"
echo "   Command: grpcurl -plaintext localhost:9090 list"
echo "   Expected logs: gRPC request/response for service listing"
grpcurl -plaintext localhost:9090 list > /dev/null 2>&1
echo "   ✅ Request sent - check logs!"
echo ""

echo ""
echo "🔍 Log Monitoring Commands"
echo "=========================="
echo ""
echo "📊 All HTTP REST logs:"
echo "   tail -f app.log | grep --color=always -E '(🌐|HTTP|Request|Response)'"
echo ""
echo "🚀 All gRPC logs:"
echo "   tail -f app.log | grep --color=always -E '(🚀|gRPC|RPC)'"
echo ""
echo "🔍 Service layer logs:"
echo "   tail -f app.log | grep --color=always -E '(🔍|✅|❌|Service:)'"
echo ""
echo "📋 Request/Response bodies:"
echo "   tail -f app.log | grep --color=always -E '(📥|📤|Body:)'"
echo ""
echo "⏱️  Performance logs:"
echo "   tail -f app.log | grep --color=always -E '(Duration|⏱️)'"
echo ""

echo "🎯 What You Should See in Logs:"
echo "==============================="
echo ""
echo "For HTTP REST requests:"
echo "  🌐 HTTP REQUEST with method, URL, headers"
echo "  📥 Request Body (for POST/PUT)"
echo "  🔍 Service method calls with parameters"
echo "  ✅ Service results"
echo "  📤 Response Body"
echo "  🌐 HTTP RESPONSE with status and timing"
echo ""
echo "For gRPC requests:"
echo "  🚀 gRPC REQUEST with method name"
echo "  📥 gRPC Request Message"
echo "  ✅ gRPC Request completed"
echo "  📤 gRPC Response Message"
echo "  🚀 gRPC RESPONSE with status and timing"
echo ""

echo "🛠️  Additional Testing:"
echo "======================="
echo ""
echo "Manual test commands:"
echo ""
echo "# HTTP REST with verbose curl:"
echo "curl -v http://localhost:8085/api/products/count"
echo ""
echo "# gRPC with verbose grpcurl:"
echo "grpcurl -v -plaintext localhost:9090 grpc.health.v1.Health/Check"
echo ""
echo "# Real-time monitoring:"
echo "./monitor_message_exchanges.sh"
echo ""

echo "✅ Demo complete! Check the application logs to see verbose request/response logging."
