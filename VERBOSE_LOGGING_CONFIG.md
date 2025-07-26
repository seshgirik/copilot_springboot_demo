# Comprehensive Verbose Logging Configuration

## Overview
This Spring Boot application has been configured with comprehensive verbose logging for both REST (HTTP/JSON) and gRPC (HTTP/2/Protobuf) APIs. Every request, response, and business operation is logged with detailed information.

## Logging Components

### 1. Application-wide Logging Configuration (`application.yml`)

```yaml
logging:
  level:
    com.demo.springboot: DEBUG                           # Our application code
    org.hibernate.SQL: DEBUG                            # SQL queries
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE # SQL parameters
    # gRPC verbose logging
    io.grpc: DEBUG                                       # gRPC framework
    net.devh.boot.grpc: DEBUG                           # gRPC Spring Boot integration
    grpc: DEBUG                                          # General gRPC
    # HTTP/REST verbose logging
    org.springframework.web: DEBUG                       # Spring Web framework
    org.springframework.web.servlet.mvc.method.annotation: DEBUG # REST controllers
    org.springframework.web.servlet.DispatcherServlet: DEBUG     # Request dispatching
    # HTTP client logging
    org.apache.http: DEBUG                               # HTTP client operations
    # Netty logging for gRPC
    io.netty: DEBUG                                      # Netty (used by gRPC)
    # Request/Response logging
    web: DEBUG                                           # Web-related logging
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 2. HTTP Request/Response Logging

#### `HttpLoggingInterceptor.java`
- **Purpose**: Logs all HTTP requests and responses
- **Logs**: Method, URI, headers, execution time
- **Level**: INFO for request/response, DEBUG for details

#### `HttpBodyLoggingFilter.java`
- **Purpose**: Logs HTTP request and response bodies
- **Logs**: Complete request/response payloads
- **Level**: DEBUG (to avoid overwhelming logs)

#### `WebMvcConfig.java` & `FilterConfig.java`
- **Purpose**: Registers HTTP logging interceptors and filters
- **Configuration**: Applies to all REST endpoints

### 3. gRPC Request/Response Logging

#### `GrpcLoggingInterceptor.java`
- **Purpose**: Logs all gRPC method calls and responses
- **Logs**: Method name, request/response messages, execution time
- **Level**: INFO for calls, DEBUG for message details
- **Features**: Automatically registered for all gRPC services

### 4. Service Layer Logging

#### `ProductService.java`
- **Enhanced with**: Comprehensive logging for all CRUD operations
- **Logs**:
  - Method entry with parameters
  - Business logic decisions
  - Database operation results
  - Method exit with results
  - Error conditions with context

#### `UserService.java`
- **Enhanced with**: Detailed logging for all user operations
- **Logs**:
  - User creation, update, deletion
  - Search operations with criteria
  - Validation results
  - Business rule enforcement

### 5. Controller Layer Logging

#### `ProductController.java`
- **Enhanced with**: REST endpoint logging
- **Logs**:
  - Incoming requests with parameters
  - Response preparation
  - Error handling

#### `UserController.java`
- **Enhanced with**: User API endpoint logging
- **Logs**:
  - HTTP method and endpoint
  - Request parameters
  - Response status and data

### 6. gRPC Service Implementation Logging

#### `UserGrpcServiceImpl.java`
- **Enhanced with**: Full gRPC service logging
- **Logs**:
  - gRPC method invocations
  - Request/response conversion
  - Business logic delegation
  - Error handling and status codes

#### `ProductGrpcServiceImpl.java`
- **Enhanced with**: Complete product gRPC logging
- **Logs**:
  - All CRUD operations via gRPC
  - Protobuf message handling
  - Service integration
  - Exception management

## Log Output Examples

### REST API Request Example
```
2025-07-12 10:30:15 [http-nio-8085-exec-1] INFO  c.d.s.config.HttpLoggingInterceptor - 🌐 HTTP Request: POST /api/products
2025-07-12 10:30:15 [http-nio-8085-exec-1] DEBUG c.d.s.config.HttpBodyLoggingFilter - 📝 Request Body: {"name":"Test Product","price":99.99}
2025-07-12 10:30:15 [http-nio-8085-exec-1] INFO  c.d.s.controller.ProductController - 🌐 REST: POST /api/products - creating product: Test Product
2025-07-12 10:30:15 [http-nio-8085-exec-1] INFO  c.d.s.service.ProductService - 🆕 Service: Creating new product: Test Product
2025-07-12 10:30:15 [http-nio-8085-exec-1] DEBUG org.hibernate.SQL - insert into product (description, name, price, quantity) values (?, ?, ?, ?)
2025-07-12 10:30:15 [http-nio-8085-exec-1] INFO  c.d.s.service.ProductService - ✅ Service: Product created with ID: 1
2025-07-12 10:30:15 [http-nio-8085-exec-1] INFO  c.d.s.controller.ProductController - 🌐 REST: Product created successfully with ID: 1
2025-07-12 10:30:15 [http-nio-8085-exec-1] INFO  c.d.s.config.HttpLoggingInterceptor - 🌐 HTTP Response: 201 CREATED (45ms)
```

### gRPC Request Example
```
2025-07-12 10:31:20 [grpc-default-executor-0] INFO  c.d.s.config.GrpcLoggingInterceptor - 🔌 gRPC Call: userservice.UserService/CreateUser
2025-07-12 10:31:20 [grpc-default-executor-0] DEBUG c.d.s.config.GrpcLoggingInterceptor - 🔌 gRPC Request: name: "John Doe", email: "john@example.com"
2025-07-12 10:31:20 [grpc-default-executor-0] INFO  c.d.s.grpc.service.UserGrpcServiceImpl - 🔌 gRPC: CreateUser called - name: 'John Doe', email: 'john@example.com'
2025-07-12 10:31:20 [grpc-default-executor-0] INFO  c.d.s.service.UserService - 🆕 Service: Creating new user: John Doe (john@example.com)
2025-07-12 10:31:20 [grpc-default-executor-0] DEBUG org.hibernate.SQL - insert into user (email, name, phone) values (?, ?, ?)
2025-07-12 10:31:20 [grpc-default-executor-0] INFO  c.d.s.service.UserService - ✅ Service: User created successfully with ID: 1
2025-07-12 10:31:20 [grpc-default-executor-0] INFO  c.d.s.grpc.service.UserGrpcServiceImpl - ✅ gRPC: CreateUser successful - ID: 1, Name: 'John Doe'
2025-07-12 10:31:20 [grpc-default-executor-0] INFO  c.d.s.config.GrpcLoggingInterceptor - 🔌 gRPC Response: OK (32ms)
```

## Log Levels Used

| Component | Level | Purpose |
|-----------|-------|---------|
| HTTP Requests/Responses | INFO | Track all API calls |
| HTTP Bodies | DEBUG | Detailed payload inspection |
| gRPC Calls | INFO | Track all gRPC method invocations |
| gRPC Messages | DEBUG | Detailed message content |
| Service Methods | INFO | Business logic tracking |
| Service Details | DEBUG | Detailed operation context |
| Database Queries | DEBUG | SQL execution tracking |
| Database Parameters | TRACE | SQL parameter values |

## Monitoring Commands

### View Live Logs
```bash
# Follow application logs
tail -f nohup.out

# Filter for specific components
tail -f nohup.out | grep "🌐\|🔌"  # HTTP & gRPC only
tail -f nohup.out | grep "Service:" # Service layer only
tail -f nohup.out | grep "SQL"     # Database queries only
```

### Search Logs
```bash
# Search for specific operations
grep "CreateProduct" nohup.out
grep "gRPC:" nohup.out
grep "REST:" nohup.out

# Search by log level
grep "ERROR" nohup.out
grep "WARN" nohup.out
```

## Performance Considerations

- **Production Usage**: Consider reducing log levels to WARN/ERROR for production
- **Log Rotation**: Implement log rotation for long-running applications
- **Filtering**: Use log filtering for specific debugging scenarios
- **Storage**: Monitor disk space usage with verbose logging enabled

## Architecture Integration

This verbose logging system is integrated into the single Spring Boot application that serves both protocols:

- **Shared Service Layer**: Common business logic with unified logging
- **Dual Protocol Support**: Separate logging for REST and gRPC concerns
- **Consistent Format**: Unified log format across all components
- **Performance Tracking**: Request timing and performance metrics

## Testing the Logging

Run the demonstration script to see all logging in action:
```bash
./enhanced_verbose_logging_demo.sh
```

This will execute a comprehensive set of operations and demonstrate the complete logging capability of the system.
