#!/bin/bash

# 🎯 Quick Demo: HTTP vs gRPC Message Exchanges

echo "🚀 Demo: How to See Message Exchanges"
echo "====================================="

# Wait for servers to start
echo "⏳ Waiting for servers to start..."
sleep 15

echo ""
echo "1️⃣ HTTP REST Message Exchange (JSON over HTTP)"
echo "-----------------------------------------------"
echo "🔗 URL: http://localhost:8085/api/products/count"
echo "📡 Protocol: HTTP/1.1"
echo "📦 Format: JSON"
echo ""

echo "▶️  Running curl with verbose output:"
curl -v -s http://localhost:8085/api/products/count 2>&1 | head -20

echo ""
echo ""
echo "2️⃣ gRPC Message Exchange (Protobuf over HTTP/2)"
echo "-----------------------------------------------"
echo "🔗 URL: localhost:9090"
echo "📡 Protocol: HTTP/2 (gRPC)"
echo "📦 Format: Protocol Buffers"
echo ""

echo "▶️  Running grpcurl with verbose output:"
grpcurl -v -plaintext localhost:9090 grpc.health.v1.Health/Check 2>&1 | head -20

echo ""
echo ""
echo "3️⃣ Application Logs for Message Monitoring"
echo "-------------------------------------------"
echo "▶️  Recent HTTP and gRPC logs:"
tail -10 app.log | grep -E "(GET|POST|grpc|rpc|8085|9090)" || echo "No recent message logs found"

echo ""
echo ""
echo "🛠️  Tools for Real-time Monitoring:"
echo "======================================"
echo ""
echo "📊 Method 1: Monitor HTTP REST messages"
echo "   tail -f app.log | grep -E '(GET|POST|PUT|DELETE)'"
echo ""
echo "🚀 Method 2: Monitor gRPC messages"  
echo "   tail -f app.log | grep -i grpc"
echo ""
echo "🔍 Method 3: Interactive monitoring"
echo "   ./monitor_message_exchanges.sh"
echo ""
echo "📡 Method 4: Network-level monitoring"
echo "   sudo tcpdump -i lo0 port 8085 or port 9090"
echo ""
echo "🖥️  Method 5: GUI tools"
echo "   - Wireshark (network analysis)"
echo "   - Postman (HTTP REST + gRPC)"
echo "   - BloomRPC (gRPC-specific)"

echo ""
echo "✅ Demo complete! Both servers are ready for testing."
