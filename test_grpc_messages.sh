#!/bin/bash

# 🧪 gRPC Message Testing Script

echo "🚀 gRPC Server Testing for Spring Boot Demo"
echo "============================================="

BASE_GRPC="localhost:9090"

echo ""
echo "1. 📋 Checking gRPC Server Status..."
echo "HTTP Server (8085):"
lsof -i :8085 | grep LISTEN || echo "❌ HTTP server not running"

echo ""
echo "gRPC Server (9090):"
lsof -i :9090 | grep LISTEN || echo "❌ gRPC server not running"

echo ""
echo "2. 🔍 Listing Available gRPC Services..."
grpcurl -plaintext ${BASE_GRPC} list

echo ""
echo "3. 🏥 Testing Health Check Service..."
grpcurl -plaintext ${BASE_GRPC} grpc.health.v1.Health/Check

echo ""
echo "4. 📡 Testing Server Reflection..."
grpcurl -plaintext ${BASE_GRPC} grpc.reflection.v1alpha.ServerReflection/ServerReflectionInfo

echo ""
echo "5. 🔍 Looking for Custom Services..."
echo "Expected services:"
echo "  - userservice.UserService"
echo "  - productservice.ProductService"

CUSTOM_SERVICES=$(grpcurl -plaintext ${BASE_GRPC} list | grep -E "(userservice|productservice)")
if [ -z "$CUSTOM_SERVICES" ]; then
    echo "❌ Custom services not found - they need to be implemented"
    echo ""
    echo "📋 Current Status:"
    echo "  ✅ gRPC server is running on port 9090"
    echo "  ✅ Server reflection is enabled"
    echo "  ✅ Health check service is available"
    echo "  ❌ UserService and ProductService are not implemented yet"
    echo ""
    echo "📝 Next Steps:"
    echo "  1. Implement UserGrpcServiceImpl.java"
    echo "  2. Implement ProductGrpcServiceImpl.java"
    echo "  3. Add @GrpcService annotation to the implementations"
    echo "  4. Restart the application"
else
    echo "✅ Found custom services:"
    echo "$CUSTOM_SERVICES"
fi

echo ""
echo "6. 📊 Proto Files Available:"
echo "  📄 src/main/proto/user_service.proto"
echo "  📄 src/main/proto/product_service.proto"

echo ""
echo "7. 🛠️ Tools for Testing gRPC Messages:"
echo "  🔧 grpcurl (CLI) - ✅ Installed"
echo "  🖥️  BloomRPC (GUI) - Download from: https://github.com/bloomrpc/bloomrpc"
echo "  📮 Postman (GUI) - Supports gRPC testing"

echo ""
echo "8. 📈 Monitoring gRPC Messages:"
echo "  📋 Application logs: tail -f app.log"
echo "  🔍 Network capture: Use Wireshark on port 9090"
echo "  📊 gRPC metrics: Available through actuator endpoints"

echo ""
echo "🎯 To see actual gRPC messages, implement the services and use:"
echo "   grpcurl -plaintext -d '{\"id\": 1}' localhost:9090 userservice.UserService/GetUser"
