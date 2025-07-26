# 🔍 How to See Message Exchanges: HTTP REST vs gRPC

## 🎯 Key Difference

**curl commands** → HTTP REST API (port 8085) → JSON messages  
**grpcurl commands** → gRPC API (port 9090) → Protocol Buffer messages

## 📡 Method 1: See HTTP REST Message Exchanges (curl)

### Enable Verbose curl Output
```bash
# See full HTTP request/response headers and timing
curl -v http://localhost:8085/api/products/count

# See HTTP status code
curl -w "\nHTTP Status: %{http_code}\nTotal Time: %{time_total}s\n" \
  http://localhost:8085/api/products/count

# Save response headers to file
curl -D headers.txt http://localhost:8085/api/products/count
```

### Monitor HTTP Traffic with Network Tools
```bash
# Using tcpdump (macOS/Linux)
sudo tcpdump -i lo0 -A port 8085

# Using netstat to see connections
netstat -an | grep 8085
```

### Real-time HTTP Request Monitoring
```bash
# Monitor application logs for HTTP requests
tail -f app.log | grep -E "(GET|POST|PUT|DELETE|PATCH)"

# Monitor with timestamps
tail -f app.log | while read line; do echo "$(date): $line"; done
```

## 🚀 Method 2: See gRPC Message Exchanges

### Enable gRPC Logging (Already Added)
The application now logs gRPC messages at DEBUG level.

### Monitor gRPC Messages in Real-time
```bash
# Monitor gRPC-specific logs
tail -f app.log | grep -i grpc

# Monitor gRPC with colors (if you have colorlog)
tail -f app.log | grep --color=always -E "(grpc|GRPC|rpc)"
```

### Use grpcurl with Verbose Output
```bash
# Test gRPC with verbose output
grpcurl -v -plaintext localhost:9090 grpc.health.v1.Health/Check

# List services with detailed info
grpcurl -plaintext localhost:9090 list -v

# Once custom services are implemented:
grpcurl -v -plaintext -d '{"id": 1}' localhost:9090 userservice.UserService/GetUser
```

### Monitor gRPC Network Traffic
```bash
# Capture HTTP/2 traffic (gRPC uses HTTP/2)
sudo tcpdump -i lo0 -A port 9090

# Monitor gRPC connections
lsof -i :9090 -r 1  # Refresh every second
```

## 📊 Method 3: Side-by-Side Monitoring Setup

Create multiple terminal windows to monitor both simultaneously:

### Terminal 1: HTTP REST Monitoring
```bash
cd /Users/sekondav/Downloads/copilot_springboot_demo
echo "🌐 HTTP REST API Monitoring (Port 8085)"
tail -f app.log | grep -E "(GET|POST|PUT|DELETE|PATCH|8085)" --color=always
```

### Terminal 2: gRPC Monitoring  
```bash
cd /Users/sekondav/Downloads/copilot_springboot_demo
echo "🚀 gRPC API Monitoring (Port 9090)"
tail -f app.log | grep -iE "(grpc|rpc|9090)" --color=always
```

### Terminal 3: Test Commands
```bash
# Test HTTP REST
curl -v http://localhost:8085/api/products/count

# Test gRPC
grpcurl -v -plaintext localhost:9090 grpc.health.v1.Health/Check
```

## 🛠️ Method 4: Advanced Message Interception

### Create HTTP Request Logger
```bash
# Create a simple HTTP proxy to log requests
cat > http_logger.sh << 'EOF'
#!/bin/bash
echo "=== HTTP Request at $(date) ==="
curl -v "$@" 2>&1 | tee -a http_requests.log
echo "================================"
EOF

chmod +x http_logger.sh

# Use it instead of curl
./http_logger.sh http://localhost:8085/api/products/count
```

### Create gRPC Message Logger
```bash
# Create a gRPC request logger
cat > grpc_logger.sh << 'EOF'
#!/bin/bash
echo "=== gRPC Request at $(date) ==="
grpcurl -v "$@" 2>&1 | tee -a grpc_requests.log
echo "================================"
EOF

chmod +x grpc_logger.sh

# Use it instead of grpcurl
./grpc_logger.sh -plaintext localhost:9090 grpc.health.v1.Health/Check
```

## 📈 Method 5: Use Professional Tools

### Wireshark (GUI Network Analyzer)
```bash
# Install Wireshark
brew install --cask wireshark

# Capture traffic:
# 1. Open Wireshark
# 2. Select loopback interface (lo0)
# 3. Filter: "tcp.port == 8085 or tcp.port == 9090"
# 4. Run your curl/grpcurl commands
# 5. See detailed packet analysis
```

### Postman (GUI API Testing)
- **HTTP REST**: Import OpenAPI spec from http://localhost:8085/api-docs
- **gRPC**: Import proto files from `src/main/proto/`

### BloomRPC (gRPC-specific GUI)
```bash
# Download BloomRPC
# 1. Go to: https://github.com/bloomrpc/bloomrpc
# 2. Import proto files: src/main/proto/*.proto
# 3. Set address: localhost:9090
# 4. See real-time message exchanges
```

## 🧪 Method 6: Create a Message Exchange Test

Let me create a comprehensive test script:
