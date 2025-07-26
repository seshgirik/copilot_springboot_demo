# gRPC Setup Guide

This document explains how to complete the gRPC implementation in the Spring Boot demo project.

## Current Status

✅ **Completed:**
- Protocol Buffer definitions (`user_service.proto` and `product_service.proto`)
- Maven protobuf plugin configuration
- Generated protobuf Java classes
- gRPC dependencies in pom.xml
- Placeholder gRPC service classes

⚠️ **Pending:**
- Full gRPC service implementation
- gRPC server configuration
- gRPC client examples

## Completing the gRPC Implementation

### Step 1: Verify Generated Classes

After running `mvn compile`, check that the following classes are generated in `target/generated-sources/`:

```
target/generated-sources/protobuf/
├── java/com/demo/springboot/grpc/generated/
│   ├── User.java
│   ├── UserRequest.java
│   ├── UserResponse.java
│   ├── Product.java
│   ├── ProductRequest.java
│   └── ProductResponse.java
└── grpc-java/com/demo/springboot/grpc/generated/
    ├── UserServiceGrpc.java
    └── ProductServiceGrpc.java
```

### Step 2: Enable gRPC Server

Update `application.yml` to include gRPC configuration:

```yaml
grpc:
  server:
    port: 9090
    reflection-service-enabled: true
```

### Step 3: Complete gRPC Service Implementation

Replace the placeholder `UserGrpcServiceImpl` with the full implementation:

```java
@GrpcService
public class UserGrpcServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    
    @Autowired
    private UserService userService;
    
    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        // Implementation here
    }
    
    // Implement other methods...
}
```

### Step 4: Add gRPC Client Configuration

Create a gRPC client configuration:

```java
@Configuration
public class GrpcClientConfig {
    
    @Bean
    public NettyChannelBuilder userServiceChannel() {
        return NettyChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext();
    }
}
```

### Step 5: Testing gRPC Services

#### Using grpcurl

Install grpcurl and test the services:

```bash
# List services
grpcurl -plaintext localhost:9090 list

# Test create user
grpcurl -plaintext -d '{"name":"John Doe","email":"john@example.com","phone":"1234567890"}' \
  localhost:9090 userservice.UserService/CreateUser
```

#### Using BloomRPC or Postman

1. Import the proto files
2. Connect to `localhost:9090`
3. Test the various RPC methods

### Step 6: Add Integration Tests

Create gRPC integration tests:

```java
@SpringBootTest
@TestPropertySource(properties = {
    "grpc.server.port=0",
    "grpc.server.in-process-name=test"
})
class UserGrpcServiceIntegrationTest {
    
    @Test
    void testCreateUser() {
        // Test implementation
    }
}
```

## gRPC Features Included

### Unary RPCs
- `CreateUser`
- `GetUser`
- `UpdateUser`
- `DeleteUser`
- `GetAllUsers`
- `SearchUsers`

### Server Streaming
- `StreamUsers` - Stream multiple users based on filter

### Client Streaming
- `CreateUsersBatch` - Accept stream of user creation requests

### Bidirectional Streaming
- `ProcessUserStream` - Real-time user processing

## Protocol Buffer Definitions

### User Service (`user_service.proto`)
```protobuf
service UserService {
  rpc CreateUser(CreateUserRequest) returns (UserResponse);
  rpc GetUser(GetUserRequest) returns (UserResponse);
  rpc UpdateUser(UpdateUserRequest) returns (UserResponse);
  rpc DeleteUser(DeleteUserRequest) returns (UserDeleteResponse);
  rpc GetAllUsers(GetAllUsersRequest) returns (UsersResponse);
  rpc SearchUsers(SearchUsersRequest) returns (UsersResponse);
  rpc StreamUsers(UserStreamRequest) returns (stream User);
  rpc CreateUsersBatch(stream CreateUserRequest) returns (UserBatchResponse);
  rpc ProcessUserStream(stream CreateUserRequest) returns (stream UserResponse);
}
```

### Product Service (`product_service.proto`)
Similar structure with product-specific operations.

## Troubleshooting

### Common Issues

1. **Generated classes not found:**
   ```bash
   mvn clean compile
   ```

2. **gRPC server not starting:**
   - Check port conflicts
   - Verify gRPC dependencies
   - Check application logs

3. **Proto compilation errors:**
   - Verify proto syntax
   - Check protobuf plugin configuration
   - Ensure protoc is accessible

### Maven Commands

```bash
# Clean and regenerate protobuf classes
mvn clean compile

# Run with gRPC debug logging
mvn spring-boot:run -Dlogging.level.io.grpc=DEBUG

# Package with gRPC
mvn clean package -DskipTests
```

## Next Steps

1. Uncomment gRPC configuration in `application.yml`
2. Implement the full gRPC service methods
3. Add gRPC client examples
4. Create comprehensive integration tests
5. Add gRPC interceptors for logging/security
6. Implement gRPC health checks

## Resources

- [gRPC Java Documentation](https://grpc.io/docs/languages/java/)
- [Spring Boot gRPC Starter](https://github.com/yidongnan/grpc-spring-boot-starter)
- [Protocol Buffers Guide](https://developers.google.com/protocol-buffers)
- [grpcurl Tool](https://github.com/fullstorydev/grpcurl)
