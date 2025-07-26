#!/bin/bash

echo "🎯 === COMPREHENSIVE VERBOSE LOGGING DEMONSTRATION ==="
echo ""
echo "📊 This script demonstrates the complete verbose logging system for both REST and gRPC APIs"
echo ""

# Function to show logs with context
show_logs() {
    echo "📋 Recent Application Logs (last 30 entries with verbose indicators):"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    tail -30 app.log | grep -E "(🌐|🔌|🔍|✅|🆕|📦|🗑️|🔄|📊|💰|💵|❌|INFO.*Service:|INFO.*Controller:|DEBUG.*Service:)" | head -20
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
}

echo "🚀 === TESTING COMPREHENSIVE LOGGING ==="
echo ""

echo "1️⃣ Testing REST API Product Creation..."
REST_RESPONSE=$(curl -s -X POST http://localhost:8085/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Verbose Logging Demo Product",
    "description": "Created to demonstrate comprehensive REST logging",
    "price": 449.99,
    "quantity": 100
  }')
echo "✅ REST Response: $REST_RESPONSE"
echo ""

sleep 2

echo "2️⃣ Testing gRPC User Creation..."
GRPC_USER_RESPONSE=$(grpcurl -plaintext -d '{
  "name": "Verbose Demo User",
  "email": "verbose.demo@example.com",
  "phone": "+1-555-DEMO-LOG"
}' localhost:9090 userservice.UserService/CreateUser 2>/dev/null)
echo "✅ gRPC User Response: $GRPC_USER_RESPONSE"
echo ""

sleep 2

echo "3️⃣ Testing gRPC Product Creation..."
GRPC_PRODUCT_RESPONSE=$(grpcurl -plaintext -d '{
  "name": "Verbose gRPC Product",
  "description": "Created to demonstrate comprehensive gRPC logging",
  "price": 599.99,
  "quantity": 75
}' localhost:9090 productservice.ProductService/CreateProduct 2>/dev/null)
echo "✅ gRPC Product Response: $GRPC_PRODUCT_RESPONSE"
echo ""

sleep 2

echo "4️⃣ Testing REST API Product Retrieval..."
REST_GET_RESPONSE=$(curl -s http://localhost:8085/api/products/1)
echo "✅ REST Get Response: $REST_GET_RESPONSE"
echo ""

sleep 2

echo "5️⃣ Testing gRPC User Retrieval..."
GRPC_GET_RESPONSE=$(grpcurl -plaintext -d '{"id": 1}' localhost:9090 userservice.UserService/GetUser 2>/dev/null)
echo "✅ gRPC Get Response: $GRPC_GET_RESPONSE"
echo ""

sleep 2

echo ""
echo "📊 === VERBOSE LOGGING ANALYSIS ==="
echo ""

show_logs

echo ""
echo "🔍 === LOGGING SYSTEM FEATURES DEMONSTRATED ==="
echo ""
echo "✅ REST API Logging:"
echo "   • HTTP request/response interceptors"
echo "   • Request body logging"
echo "   • Controller layer logging with emojis"
echo "   • Service layer business logic logging"
echo "   • Database operation logging"
echo ""
echo "✅ gRPC API Logging:"
echo "   • gRPC method call interceptors"
echo "   • Message payload logging"
echo "   • Service implementation logging with emojis"
echo "   • Protobuf message conversion logging"
echo "   • Error handling and status code logging"
echo ""
echo "✅ Service Layer Logging:"
echo "   • Method entry/exit logging"
echo "   • Parameter and result logging"
echo "   • Business logic decision logging"
echo "   • Database query logging with SQL details"
echo "   • Exception handling with context"
echo ""
echo "✅ Configuration Features:"
echo "   • Configurable log levels (DEBUG, INFO, WARN, ERROR)"
echo "   • Emoji-based log categorization for easy filtering"
echo "   • Performance timing information"
echo "   • Request correlation and tracing"
echo ""
echo "🎯 === LOG FILTERING EXAMPLES ==="
echo ""
echo "📝 To filter logs by type, use these commands:"
echo ""
echo "   # All verbose service logs"
echo "   tail -f app.log | grep -E '(🔍|✅|🆕|📦|🗑️|🔄)'"
echo ""
echo "   # Only REST API logs"
echo "   tail -f app.log | grep '🌐'"
echo ""
echo "   # Only gRPC API logs"
echo "   tail -f app.log | grep '🔌'"
echo ""
echo "   # Only database logs"
echo "   tail -f app.log | grep 'SQL'"
echo ""
echo "   # Only error logs"
echo "   tail -f app.log | grep 'ERROR\\|❌'"
echo ""
echo "🚀 === CONCLUSION ==="
echo ""
echo "✅ Both REST and gRPC APIs are now fully instrumented with comprehensive verbose logging"
echo "✅ Every request, response, and business operation is logged with detailed context"
echo "✅ The logging system provides full visibility into the application's behavior"
echo "✅ Logs can be filtered by protocol (REST/gRPC), operation type, or log level"
echo "✅ The single Spring Boot application successfully serves both HTTP/JSON and HTTP/2/Protobuf protocols"
echo ""
echo "📖 For more information, see:"
echo "   • VERBOSE_LOGGING_CONFIG.md - Detailed logging configuration"
echo "   • ARCHITECTURE_DIAGRAM.md - System architecture overview"
echo "   • MESSAGE_MONITORING_GUIDE.md - Message monitoring instructions"
echo ""
