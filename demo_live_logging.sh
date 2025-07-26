#!/bin/bash

echo "🚀 Live Demo: gRPC and REST Verbose Logging"
echo "=============================================="
echo ""
echo "This script will:"
echo "1. Make REST API calls"
echo "2. Make gRPC calls"
echo "3. Show the real-time logs for both"
echo ""
echo "Press Enter to start the demo..."
read

# Function to check if app is running
check_app() {
    if ! curl -s http://localhost:8085/api/users > /dev/null 2>&1; then
        echo "❌ Application not running on port 8085. Please start it first:"
        echo "   mvn spring-boot:run"
        exit 1
    fi
    
    if ! grpcurl -plaintext localhost:9090 list > /dev/null 2>&1; then
        echo "❌ gRPC server not running on port 9090. Please start it first:"
        echo "   mvn spring-boot:run"
        exit 1
    fi
}

# Check if application is running
echo "🔍 Checking if application is running..."
check_app
echo "✅ Application is running!"
echo ""

# Get log file size to tail from current position
LOG_SIZE=$(wc -l < app.log 2>/dev/null || echo "0")

echo "📊 Starting live log monitoring from line $LOG_SIZE..."
echo "📝 Open another terminal and run: tail -f app.log | grep -E '(🚀|🔌|📤|📥|🔍|✅|❌|🆕|🔄|🗑️)'"
echo ""

sleep 2

echo "🌐 Making REST API call..."
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "REST User", "email": "rest@example.com", "phone": "555-1234"}'

echo ""
echo ""

sleep 2

echo "🔌 Making gRPC call..."
grpcurl -plaintext -d '{"name": "gRPC User", "email": "grpc@example.com", "phone": "555-5678"}' \
  localhost:9090 userservice.UserService/CreateUser

echo ""
echo ""

sleep 2

echo "🔍 Recent logs showing both REST and gRPC activity:"
echo "=================================================="
tail -n 30 app.log | grep -E "(🚀|🔌|📤|📥|🔍|✅|❌|🆕|🔄|🗑️|HTTP|gRPC)"

echo ""
echo "✅ Demo complete! Both REST and gRPC verbose logging are working!"
echo ""
echo "💡 To see live logs in real-time:"
echo "   tail -f app.log"
echo ""
echo "💡 To filter for just gRPC logs:"
echo "   tail -f app.log | grep gRPC"
echo ""
echo "💡 To filter for just REST logs:"
echo "   tail -f app.log | grep -E '(HTTP|REST|Controller|Filter)'"
