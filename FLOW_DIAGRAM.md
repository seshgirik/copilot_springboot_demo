# Spring Boot gRPC REST Demo - Request Flow Diagram

## Complete Request Flow Architecture

This document provides a comprehensive flow diagram for GET and POST operations in the Spring Boot gRPC REST Demo application.

## 🔄 **GET Request Flow** (e.g., `GET /api/users/1`)

```
📱 CLIENT REQUEST
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 1. HTTP REQUEST ARRIVES                                     │
│    GET /api/users/1                                         │
│    Headers: Accept: application/json                       │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. TOMCAT EMBEDDED SERVER                                   │
│    📡 Port 8085 (configured in application.yml)            │
│    🔧 Handles HTTP protocol, connection pooling            │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. SPRING SECURITY FILTER CHAIN                            │
│    🔐 JwtAuthenticationFilter                              │
│    📋 SecurityConfig.java                                  │
│    • Extract JWT token from Authorization header           │
│    • Validate token using JwtUtil                          │
│    • Set SecurityContext with user details                 │
│    • Continue if authorized, return 401 if not             │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. HTTP LOGGING FILTER                                     │
│    📝 HttpBodyLoggingFilter                                │
│    📊 FilterConfig.java                                    │
│    • Log incoming request details                          │
│    • Log request headers, method, URI                      │
│    • Continue processing                                   │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. SPRING MVC DISPATCHER SERVLET                           │
│    🎯 DispatcherServlet                                    │
│    🗺️ WebMvcConfig.java                                     │
│    • Map request to appropriate controller                 │
│    • Handle CORS configuration                             │
│    • Route to @RequestMapping("/api/users")                │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 6. CONTROLLER LAYER                                        │
│    🎮 UserController.java                                  │
│    📍 @GetMapping("/{id}")                                 │
│    • Extract path variable: Long id = 1                    │
│    • Log request: "🌐 REST: GET /api/users/1"              │
│    • Call service layer                                    │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 7. SERVICE LAYER                                           │
│    🏢 UserService.java                                     │
│    📊 @Service annotation                                  │
│    • Business logic processing                             │
│    • getUserById(Long id) method                           │
│    • Log: "🔍 Service: Getting user by ID: 1"              │
│    • Call repository layer                                 │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 8. REPOSITORY LAYER                                        │
│    💾 UserRepository.java (extends JpaRepository)          │
│    🔍 Spring Data JPA                                      │
│    • Generate SQL: SELECT * FROM users WHERE id = 1       │
│    • Execute database query                                │
│    • Return Optional<User>                                 │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 9. DATABASE LAYER                                          │
│    🗄️ H2 In-Memory Database                                 │
│    🔗 HikariCP Connection Pool                             │
│    • Execute SQL query                                     │
│    • Fetch user record if exists                           │
│    • Return result set                                     │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 10. DATA MAPPING & RETURN FLOW                             │
│     🔄 JPA Entity Mapping                                  │
│     📦 User.java entity                                    │
│     • Map database row to User entity                      │
│     • Apply validation annotations                         │
│     • Return to service layer                              │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 11. SERVICE RESPONSE PROCESSING                            │
│     🏢 UserService.java                                    │
│     • Check if user exists                                 │
│     • Log: "✅ Service: Found user: John (john@test.com)"   │
│     • Return Optional<User> to controller                  │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 12. CONTROLLER RESPONSE HANDLING                           │
│     🎮 UserController.java                                 │
│     • Check if user.isPresent()                            │
│     • If found: ResponseEntity.ok(user.get()) → 200 OK     │
│     • If not found: ResponseEntity.notFound() → 404        │
│     • Log: "🌐 REST: Returning user: John (john@test.com)" │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 13. JSON SERIALIZATION                                     │
│     📄 Jackson ObjectMapper                                │
│     🔧 Auto-configured by Spring Boot                      │
│     • Convert User entity to JSON                          │
│     • Apply @JsonIgnore annotations                        │
│     • Set Content-Type: application/json                   │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 14. HTTP RESPONSE                                          │
│     📡 Tomcat Server                                       │
│     • Send HTTP 200 OK                                     │
│     • Headers: Content-Type: application/json             │
│     • Body: {"id":1,"name":"John","email":"john@test.com"} │
└─────────────────────────────────────────────────────────────┘
        ↓
📱 CLIENT RECEIVES RESPONSE
```

---

## 🔄 **POST Request Flow** (e.g., `POST /api/users`)

```
📱 CLIENT REQUEST
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 1. HTTP REQUEST ARRIVES                                     │
│    POST /api/users                                          │
│    Headers: Content-Type: application/json                 │
│    Body: {"name":"Jane","email":"jane@test.com","phone":"123"} │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. TOMCAT EMBEDDED SERVER                                   │
│    📡 Port 8085 (configured in application.yml)            │
│    🔧 Parse HTTP request, handle body                       │
│    📦 Buffer request body for processing                    │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. SPRING SECURITY FILTER CHAIN                            │
│    🔐 JwtAuthenticationFilter                              │
│    📋 SecurityConfig.java                                  │
│    • Extract JWT token from Authorization header           │
│    • Validate token using JwtUtil                          │
│    • Check user roles/permissions for POST operation       │
│    • Set SecurityContext, continue if authorized           │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. HTTP LOGGING FILTER                                     │
│    📝 HttpBodyLoggingFilter                                │
│    📊 FilterConfig.java                                    │
│    • Log incoming POST request details                     │
│    • Log request body content                              │
│    • Continue processing                                   │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. SPRING MVC DISPATCHER SERVLET                           │
│    🎯 DispatcherServlet                                    │
│    🗺️ WebMvcConfig.java                                     │
│    • Route to @RequestMapping("/api/users")                │
│    • Identify @PostMapping method                          │
│    • Prepare for JSON deserialization                      │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 6. JSON DESERIALIZATION                                    │
│    📄 Jackson ObjectMapper                                 │
│    🔧 Auto-configured by Spring Boot                       │
│    • Parse JSON from request body                          │
│    • Map to User.java entity object                        │
│    • Apply constructor/setter mapping                      │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 7. BEAN VALIDATION                                         │
│    ✅ @Valid annotation processing                         │
│    📋 Jakarta Validation (JSR-380)                         │
│    • Validate @NotBlank on name field                      │
│    • Validate @Email on email field                        │
│    • Validate @Size constraints                            │
│    • If invalid: return 400 Bad Request with errors        │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 8. CONTROLLER LAYER                                        │
│    🎮 UserController.java                                  │
│    📍 @PostMapping method                                  │
│    • Receive validated User object                         │
│    • Log: "🌐 REST: POST /api/users - creating user"       │
│    • Call service layer with user data                     │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 9. SERVICE LAYER                                           │
│    🏢 UserService.java                                     │
│    📊 @Service annotation                                  │
│    • Business logic: createUser(User user)                 │
│    • Check if email already exists                         │
│    • Log: "🆕 Service: Creating new user: Jane"            │
│    • Call repository to save                               │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 10. BUSINESS VALIDATION                                     │
│     🏢 UserService.java                                    │
│     • userRepository.existsByEmail(user.getEmail())        │
│     • If exists: throw RuntimeException                    │
│     • If valid: proceed to save operation                  │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 11. REPOSITORY LAYER                                       │
│     💾 UserRepository.java (extends JpaRepository)         │
│     🔍 Spring Data JPA                                     │
│     • Generate INSERT SQL statement                        │
│     • Execute: INSERT INTO users (name, email, phone)      │
│     • Return saved User entity with generated ID           │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 12. DATABASE TRANSACTION                                   │
│     🗄️ H2 In-Memory Database                               │
│     🔗 HikariCP Connection Pool                            │
│     📋 @Transactional handling                             │
│     • Begin transaction                                    │
│     • Execute INSERT statement                             │
│     • Auto-generate ID, commit transaction                 │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 13. SERVICE RESPONSE PROCESSING                            │
│     🏢 UserService.java                                    │
│     • Receive saved User entity from repository            │
│     • Log: "✅ Service: User created with ID: 1"           │
│     • Return User entity to controller                     │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 14. CONTROLLER RESPONSE HANDLING                           │
│     🎮 UserController.java                                 │
│     • Receive created User from service                    │
│     • Return ResponseEntity.status(CREATED).body(user)     │
│     • HTTP Status: 201 Created                             │
│     • Exception handling for duplicate email               │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 15. JSON SERIALIZATION                                     │
│     📄 Jackson ObjectMapper                                │
│     🔧 Auto-configured by Spring Boot                      │
│     • Convert created User entity to JSON                  │
│     • Include generated ID in response                     │
│     • Set Content-Type: application/json                   │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 16. HTTP RESPONSE                                          │
│     📡 Tomcat Server                                       │
│     • Send HTTP 201 Created                                │
│     • Headers: Content-Type: application/json             │
│     • Body: {"id":1,"name":"Jane","email":"jane@test.com"} │
└─────────────────────────────────────────────────────────────┘
        ↓
📱 CLIENT RECEIVES RESPONSE
```

---

## 🏗️ **Architecture Components Overview**

### 📊 **Component Layers**

```
┌─────────────────────────────────────────────────────────────┐
│ 🌐 PRESENTATION LAYER                                       │
│ • UserController.java                                      │
│ • ProductController.java                                   │
│ • AuthController.java                                      │
│ • @RestController, @RequestMapping                         │
│ • HTTP status codes, ResponseEntity                        │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 🏢 BUSINESS LAYER                                           │
│ • UserService.java                                         │
│ • ProductService.java                                      │
│ • CustomUserDetailsService.java                            │
│ • @Service, business logic, validation                     │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 💾 PERSISTENCE LAYER                                        │
│ • UserRepository.java                                      │
│ • ProductRepository.java                                   │
│ • JpaRepository, Spring Data JPA                           │
│ • Custom query methods                                     │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 🗄️ DATA LAYER                                               │
│ • User.java entity                                         │
│ • Product.java entity                                      │
│ • H2 Database                                              │
│ • JPA annotations, relationships                           │
└─────────────────────────────────────────────────────────────┘
```

### 🔧 **Cross-Cutting Concerns**

```
┌─────────────────────────────────────────────────────────────┐
│ 🔐 SECURITY                                                 │
│ • SecurityConfig.java                                      │
│ • JwtAuthenticationFilter.java                             │
│ • JwtUtil.java                                             │
│ • JWT token validation, CORS                               │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ 📝 LOGGING & MONITORING                                     │
│ • HttpBodyLoggingFilter.java                               │
│ • HttpLoggingInterceptor.java                              │
│ • GrpcLoggingInterceptor.java                              │
│ • SLF4J logging throughout application                     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ ✅ VALIDATION                                               │
│ • @Valid annotations                                       │
│ • Jakarta Bean Validation                                  │
│ • Custom validation in service layer                       │
│ • Entity-level constraints                                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ 🔄 TRANSACTION MANAGEMENT                                   │
│ • @Transactional annotations                               │
│ • Spring transaction management                            │
│ • Database connection pooling                              │
│ • Rollback on exceptions                                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚦 **Error Handling Flow**

### ❌ **Error Scenarios & Responses**

```
┌─────────────────────────────────────────────────────────────┐
│ 🚨 VALIDATION ERRORS (400 Bad Request)                     │
│ • @Valid annotation fails                                  │
│ • Jakarta Bean Validation                                  │
│ • Return error details in response body                    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ 🔐 AUTHENTICATION ERRORS (401 Unauthorized)                │
│ • Invalid JWT token                                        │
│ • Missing Authorization header                             │
│ • Token expired                                            │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ 🚫 RESOURCE NOT FOUND (404 Not Found)                      │
│ • User/Product not found by ID                             │
│ • Repository returns empty Optional                        │
│ • Controller returns ResponseEntity.notFound()             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ ⚠️ BUSINESS LOGIC ERRORS (409 Conflict)                    │
│ • Email already exists                                     │
│ • Service layer throws RuntimeException                    │
│ • Controller catches and returns appropriate status        │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 **gRPC Service Flow** (Alternative to REST)

```
📱 gRPC CLIENT REQUEST
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 1. gRPC SERVER (Port 9090)                                 │
│    🔧 Protocol Buffers                                     │
│    📡 UserServiceImpl.java                                 │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. gRPC LOGGING INTERCEPTOR                                │
│    📝 GrpcLoggingInterceptor.java                          │
│    • Log method calls and responses                        │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. gRPC SERVICE IMPLEMENTATION                             │
│    🏢 UserGrpcServiceImpl.java                             │
│    • Convert protobuf messages to entities                 │
│    • Call same UserService.java                            │
│    • Convert entities back to protobuf                     │
└─────────────────────────────────────────────────────────────┘
        ↓
[Same Service → Repository → Database flow as REST]
```

---

## 📋 **Key Files & Their Roles**

| Component | File | Purpose |
|-----------|------|---------|
| **Controllers** | `UserController.java`<br>`ProductController.java`<br>`AuthController.java` | REST API endpoints, HTTP handling |
| **Services** | `UserService.java`<br>`ProductService.java`<br>`CustomUserDetailsService.java` | Business logic, validation |
| **Repositories** | `UserRepository.java`<br>`ProductRepository.java` | Data access, JPA queries |
| **Entities** | `User.java`<br>`Product.java` | JPA entities, database mapping |
| **Security** | `SecurityConfig.java`<br>`JwtAuthenticationFilter.java`<br>`JwtUtil.java` | Authentication, authorization |
| **Configuration** | `WebMvcConfig.java`<br>`FilterConfig.java` | MVC configuration, filters |
| **Logging** | `HttpBodyLoggingFilter.java`<br>`HttpLoggingInterceptor.java` | Request/response logging |
| **gRPC** | `UserGrpcServiceImpl.java`<br>`ProductGrpcServiceImpl.java` | gRPC service implementations |

---

## 🎯 **Request Processing Summary**

### GET Request Pattern:
1. **Tomcat** receives HTTP request
2. **Security Filter** validates JWT token  
3. **Logging Filter** logs request details
4. **DispatcherServlet** routes to controller
5. **Controller** extracts parameters, calls service
6. **Service** implements business logic, calls repository
7. **Repository** executes database query
8. **Database** returns data
9. **Response** flows back through layers with JSON serialization

### POST Request Pattern:
1. **Tomcat** receives HTTP request with body
2. **Security Filter** validates JWT token
3. **Logging Filter** logs request and body
4. **JSON Deserialization** converts body to entity
5. **Bean Validation** validates entity fields
6. **Controller** processes validated entity, calls service
7. **Service** validates business rules, calls repository
8. **Repository** saves entity to database
9. **Database** persists data, returns saved entity
10. **Response** returns created entity with 201 status

This flow ensures proper separation of concerns, security, validation, and logging throughout the request lifecycle.
