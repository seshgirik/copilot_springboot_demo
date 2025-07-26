#!/bin/bash

# 🔍 Real-time Message Exchange Monitor
# This script shows both HTTP REST and gRPC message exchanges

echo "🚀 Starting Real-time Message Exchange Monitor"
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if servers are running
echo -e "${BLUE}📡 Checking Server Status...${NC}"
HTTP_STATUS=$(lsof -i :8085 | grep LISTEN | wc -l)
GRPC_STATUS=$(lsof -i :9090 | grep LISTEN | wc -l)

if [ $HTTP_STATUS -eq 0 ]; then
    echo -e "${RED}❌ HTTP Server (8085) not running${NC}"
    exit 1
else
    echo -e "${GREEN}✅ HTTP Server (8085) running${NC}"
fi

if [ $GRPC_STATUS -eq 0 ]; then
    echo -e "${RED}❌ gRPC Server (9090) not running${NC}"
    exit 1
else
    echo -e "${GREEN}✅ gRPC Server (9090) running${NC}"
fi

echo ""
echo -e "${YELLOW}🔍 Starting Message Monitoring...${NC}"
echo "Press Ctrl+C to stop monitoring"
echo ""

# Function to test HTTP and show exchange
test_http() {
    echo -e "${BLUE}=== HTTP REST Message Exchange ===${NC}"
    echo -e "${GREEN}➤ Request:${NC} GET /api/products/count"
    
    # Capture the curl command with timing and headers
    RESPONSE=$(curl -s -w "\n📊 HTTP Status: %{http_code}\n⏱️  Response Time: %{time_total}s\n📦 Size: %{size_download} bytes\n" \
        http://localhost:8085/api/products/count 2>/dev/null)
    
    echo -e "${GREEN}➤ Response:${NC}"
    echo "$RESPONSE"
    echo ""
}

# Function to test gRPC and show exchange  
test_grpc() {
    echo -e "${BLUE}=== gRPC Message Exchange ===${NC}"
    echo -e "${GREEN}➤ Request:${NC} grpc.health.v1.Health/Check"
    
    # Capture grpcurl response
    RESPONSE=$(grpcurl -plaintext localhost:9090 grpc.health.v1.Health/Check 2>/dev/null)
    
    echo -e "${GREEN}➤ Response:${NC}"
    echo "$RESPONSE"
    echo ""
}

# Function to monitor logs in background
monitor_logs() {
    echo -e "${YELLOW}📋 Application Logs (last 5 lines):${NC}"
    tail -5 app.log | while read line; do
        if [[ $line == *"GET"* ]] || [[ $line == *"POST"* ]]; then
            echo -e "${GREEN}HTTP: $line${NC}"
        elif [[ $line == *"grpc"* ]] || [[ $line == *"GRPC"* ]]; then
            echo -e "${BLUE}gRPC: $line${NC}"
        else
            echo "     $line"
        fi
    done
    echo ""
}

# Main monitoring loop
while true; do
    clear
    echo -e "${YELLOW}🔍 Real-time Message Exchange Monitor${NC}"
    echo "======================================="
    echo "$(date)"
    echo ""
    
    # Test HTTP REST API
    test_http
    
    # Test gRPC API  
    test_grpc
    
    # Show recent logs
    monitor_logs
    
    # Show network connections
    echo -e "${YELLOW}🌐 Active Connections:${NC}"
    echo "HTTP (8085): $(netstat -an | grep 8085 | grep LISTEN | wc -l) listening"
    echo "gRPC (9090): $(netstat -an | grep 9090 | grep LISTEN | wc -l) listening"
    echo ""
    
    echo -e "${YELLOW}💡 Try these commands in another terminal:${NC}"
    echo "curl -v http://localhost:8085/api/products"
    echo "grpcurl -plaintext localhost:9090 list"
    echo ""
    
    # Wait 3 seconds before next check
    sleep 3
done
