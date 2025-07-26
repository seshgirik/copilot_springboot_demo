# 🚀 gRPC Testing Guide for Spring Boot Demo

## Overview
This application includes gRPC services alongside REST APIs. Here's how to test and interact with gRPC messages.

## 📋 Available gRPC Services

### 1. **UserService** (Port: 9090)
- **Package**: `userservice`
- **Java Package**: `com.demo.springboot.grpc.generated`

#### Available RPCs:
- `CreateUser` (Unary)
- `GetUser` (Unary)  
- `UpdateUser` (Unary)
- `DeleteUser` (Unary)
- `GetAllUsers` (Unary)
- `SearchUsers` (Unary)
- `StreamUsers` (Server Streaming)
- `CreateUsersBatch` (Client Streaming)
- `ProcessUserStream` (Bidirectional Streaming)

### 2. **ProductService** (Port: 9090)
- **Package**: `productservice`
- **Java Package**: `com.demo.springboot.grpc.generated`

#### Available RPCs:
- `CreateProduct` (Unary)
- `GetProduct` (Unary)
- `UpdateProduct` (Unary)
- `DeleteProduct` (Unary)
- `GetAllProducts` (Unary)
- `SearchProducts` (Unary)
- `StreamProducts` (Server Streaming)
- `CreateProductsBatch` (Client Streaming)
- `ProcessProductStream` (Bidirectional Streaming)

---

## 🛠️ Testing Tools

### 1. **grpcurl** (Recommended)
Install grpcurl for command-line testing:

```bash
# macOS
brew install grpcurl

# Linux
curl -sSL "https://github.com/fullstorydev/grpcurl/releases/download/v1.8.9/grpcurl_1.8.9_linux_x86_64.tar.gz" | tar -xz -C /usr/local/bin/

# Windows
# Download from: https://github.com/fullstorydev/grpcurl/releases
```

### 2. **BloomRPC** (GUI Tool)
Download and install BloomRPC for a graphical interface:
- Website: https://github.com/bloomrpc/bloomrpc
- Load the proto files for interactive testing

### 3. **Postman** (GUI Tool)
Postman now supports gRPC:
- Create a new gRPC request
- Import proto files
- Set server URL to `localhost:9090`

---

## 🧪 Testing Examples with grpcurl

### Prerequisites
1. Start the application: `mvn spring-boot:run`
2. Verify gRPC server is running on port 9090
3. Ensure proto files are accessible

### List Available Services
```bash
grpcurl -plaintext localhost:9090 list
```

### List Methods for a Service
```bash
# List UserService methods
grpcurl -plaintext localhost:9090 list userservice.UserService

# List ProductService methods
grpcurl -plaintext localhost:9090 list productservice.ProductService
```

### Describe Service Methods
```bash
# Describe UserService
grpcurl -plaintext localhost:9090 describe userservice.UserService

# Describe a specific method
grpcurl -plaintext localhost:9090 describe userservice.UserService.CreateUser
```

---

## 📝 Sample gRPC Calls

### User Service Examples

#### 1. Create User (Unary)
```bash
grpcurl -plaintext -d '{
  "name": "John Doe",
  "email": "john.doe@example.com", 
  "phone": "1234567890"
}' localhost:9090 userservice.UserService/CreateUser
```

#### 2. Get User (Unary)
```bash
grpcurl -plaintext -d '{
  "id": 1
}' localhost:9090 userservice.UserService/GetUser
```

#### 3. Get All Users (Unary)
```bash
grpcurl -plaintext -d '{
  "page": 0,
  "size": 10
}' localhost:9090 userservice.UserService/GetAllUsers
```

#### 4. Search Users (Unary)
```bash
grpcurl -plaintext -d '{
  "query": "john"
}' localhost:9090 userservice.UserService/SearchUsers
```

#### 5. Stream Users (Server Streaming)
```bash
grpcurl -plaintext -d '{
  "filter": "active"
}' localhost:9090 userservice.UserService/StreamUsers
```

### Product Service Examples

#### 1. Create Product (Unary)
```bash
grpcurl -plaintext -d '{
  "name": "Test Laptop",
  "description": "High-performance laptop",
  "price": 999.99,
  "quantity": 10
}' localhost:9090 productservice.ProductService/CreateProduct
```

#### 2. Get Product (Unary)
```bash
grpcurl -plaintext -d '{
  "id": 1
}' localhost:9090 productservice.ProductService/GetProduct
```

#### 3. Search Products (Unary)
```bash
grpcurl -plaintext -d '{
  "query": "laptop",
  "min_price": 500.0,
  "max_price": 1500.0
}' localhost:9090 productservice.ProductService/SearchProducts
```

#### 4. Stream Products (Server Streaming)
```bash
grpcurl -plaintext -d '{
  "category": "electronics",
  "max_price": 1000.0
}' localhost:9090 productservice.ProductService/StreamProducts
```

---

## 🔍 Monitoring gRPC Messages

### 1. Application Logs
Enable gRPC logging in `application.yml`:
```yaml
logging:
  level:
    io.grpc: DEBUG
    net.devh.boot.grpc: DEBUG
    com.demo.springboot.grpc: DEBUG
```

### 2. gRPC Server Reflection
If server reflection is enabled, you can inspect services:
```bash
grpcurl -plaintext localhost:9090 list
```

### 3. Network Monitoring
Use tools like Wireshark to capture HTTP/2 traffic on port 9090.

---

## 🐛 Troubleshooting

### Common Issues

#### 1. "connection refused"
- Check if gRPC server is running on port 9090
- Verify firewall settings
```bash
lsof -i :9090
```

#### 2. "service not found"
- Ensure gRPC services are properly implemented
- Check if proto files are correctly compiled
- Verify service registration

#### 3. "proto file not found" 
- Check proto file paths in grpcurl commands
- Use `-import-path` and `-proto` flags if needed:
```bash
grpcurl -plaintext -import-path ./src/main/proto -proto user_service.proto localhost:9090 list
```

---

## 📁 Proto Files Location
```
src/main/proto/
├── user_service.proto
└── product_service.proto
```

---

## 🔧 Development Commands

### Restart Application with gRPC
```bash
# Stop current application
pkill -f maven

# Clean compile and restart
mvn clean compile
mvn spring-boot:run
```

### Verify gRPC Server Status
```bash
# Check if port 9090 is listening
lsof -i :9090

# Test gRPC server connectivity
grpcurl -plaintext localhost:9090 list
```

---

## 📚 Additional Resources

- **gRPC Documentation**: https://grpc.io/docs/
- **grpcurl GitHub**: https://github.com/fullstorydev/grpcurl
- **BloomRPC**: https://github.com/bloomrpc/bloomrpc
- **Spring Boot gRPC Starter**: https://github.com/yidongnan/grpc-spring-boot-starter

---

## ⚠️ Current Status

**Note**: The gRPC services in this demo are currently placeholder implementations. For full functionality:

1. Complete the gRPC service implementations in:
   - `UserGrpcServiceImpl.java`
   - `ProductGrpcServiceImpl.java` (to be created)

2. Ensure proper protobuf dependency resolution

3. Test with the provided grpcurl commands

The proto definitions are complete and ready for implementation!
