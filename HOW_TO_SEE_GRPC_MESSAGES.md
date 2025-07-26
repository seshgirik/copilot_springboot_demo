# 📡 How to See gRPC Messages in This Application

## 🎯 Quick Answer

**The application runs two servers:**
- **HTTP REST API**: `localhost:8085`
- **gRPC Server**: `localhost:9090`

## 🔍 Current Status

✅ **gRPC Server is running** on port 9090  
✅ **Server reflection is enabled**  
✅ **Health check service works**  
❌ **Custom gRPC services not yet implemented**  

## 🛠️ Tools to See gRPC Messages

### 1. **grpcurl** (Command Line) - ✅ INSTALLED
```bash
# List available services
grpcurl -plaintext localhost:9090 list

# Test health check
grpcurl -plaintext localhost:9090 grpc.health.v1.Health/Check

# Once services are implemented:
grpcurl -plaintext -d '{"id": 1}' localhost:9090 userservice.UserService/GetUser
```

### 2. **BloomRPC** (GUI Tool)
- Download: https://github.com/bloomrpc/bloomrpc
- Import proto files from `src/main/proto/`
- Connect to `localhost:9090`

### 3. **Postman** (GUI Tool)
- Create new gRPC request
- Server URL: `localhost:9090`
- Import proto files

## 📋 Available Proto Services

### UserService (`userservice.UserService`)
- CreateUser, GetUser, UpdateUser, DeleteUser
- GetAllUsers, SearchUsers
- StreamUsers (Server Streaming)
- CreateUsersBatch (Client Streaming)
- ProcessUserStream (Bidirectional Streaming)

### ProductService (`productservice.ProductService`)
- CreateProduct, GetProduct, UpdateProduct, DeleteProduct
- GetAllProducts, SearchProducts
- StreamProducts (Server Streaming)
- CreateProductsBatch (Client Streaming)
- ProcessProductStream (Bidirectional Streaming)

## 🚀 Quick Test Commands

```bash
# 1. Check gRPC server status
./test_grpc_messages.sh

# 2. List available services
grpcurl -plaintext localhost:9090 list

# 3. Health check
grpcurl -plaintext localhost:9090 grpc.health.v1.Health/Check

# 4. Monitor application logs
tail -f app.log | grep -i grpc
```

## 📈 Monitoring gRPC Traffic

### Application Logs
```bash
# Real-time gRPC logs
tail -f app.log | grep -E "(grpc|GRPC)"
```

### Network Capture
```bash
# Use Wireshark to capture HTTP/2 traffic on port 9090
# Filter: tcp.port == 9090
```

### Enable Detailed gRPC Logging
Add to `application.yml`:
```yaml
logging:
  level:
    io.grpc: DEBUG
    net.devh.boot.grpc: DEBUG
    com.demo.springboot.grpc: DEBUG
```

## 🔧 Implementation Status

**What's Working:**
- ✅ gRPC server running on port 9090
- ✅ Proto files compiled and generated
- ✅ Server reflection enabled
- ✅ Health check service

**What Needs Implementation:**
- ❌ UserGrpcServiceImpl methods
- ❌ ProductGrpcServiceImpl class and methods
- ❌ @GrpcService annotations

## 📚 Files Reference

```
📁 Proto Files:
├── src/main/proto/user_service.proto
└── src/main/proto/product_service.proto

📁 Generated Classes:
├── target/generated-sources/protobuf/java/
└── target/generated-sources/protobuf/grpc-java/

📁 Service Implementations:
├── src/main/java/.../grpc/service/UserGrpcServiceImpl.java
└── src/main/java/.../grpc/service/ProductGrpcServiceImpl.java (needs creation)

📁 Configuration:
└── src/main/resources/application.yml
```

## 🎯 Next Steps to See Real gRPC Messages

1. **Implement the gRPC services** with `@GrpcService` annotation
2. **Restart the application**
3. **Use grpcurl to test**:
   ```bash
   grpcurl -plaintext -d '{"name":"John","email":"john@test.com","phone":"123"}' \
     localhost:9090 userservice.UserService/CreateUser
   ```

## 📖 Full Documentation

- **Complete gRPC Guide**: `GRPC_TESTING_GUIDE.md`
- **Quick Test Script**: `./test_grpc_messages.sh`
- **REST API Commands**: `CURL_COMMANDS.md`
