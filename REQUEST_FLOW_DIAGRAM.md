# 🔄 Request Flow Diagram

## **HTTP REST Request Flow**

```
┌─────────────┐   1. HTTP GET /api/products/1   ┌─────────────────┐
│   Browser   │ ─────────────────────────────── │  Tomcat Server  │
│    curl     │ ◄─────────────────────────────── │   Port: 8085    │
│  Postman    │   6. JSON Response              └─────────────────┘
└─────────────┘                                          │
                                                         │ 2. Route to Controller
                                                         ▼
                                                ┌─────────────────┐
                                                │ ProductController│
                                                │ @RestController │
                                                │ @GetMapping     │
                                                └─────────────────┘
                                                         │
                                                         │ 3. Call Service
                                                         ▼
                                                ┌─────────────────┐
                                                │ ProductService  │
                                                │ @Service        │
                                                │ Business Logic  │
                                                └─────────────────┘
                                                         │
                                                         │ 4. Query Database
                                                         ▼
                                                ┌─────────────────┐
                                                │ ProductRepository│
                                                │ @Repository     │
                                                │ JPA/Hibernate   │
                                                └─────────────────┘
                                                         │
                                                         │ 5. Data
                                                         ▼
                                                ┌─────────────────┐
                                                │   H2 Database   │
                                                │   In-Memory     │
                                                └─────────────────┘
```

## **gRPC Request Flow**

```
┌─────────────┐   1. gRPC GetProduct Request    ┌─────────────────┐
│  grpcurl    │ ─────────────────────────────── │  Netty Server   │
│ BloomRPC    │ ◄─────────────────────────────── │   Port: 9090    │
│ Java Client │   6. Protobuf Response          └─────────────────┘
└─────────────┘                                          │
                                                         │ 2. Route to gRPC Service
                                                         ▼
                                                ┌─────────────────┐
                                                │ProductGrpcService│
                                                │ @GrpcService    │
                                                │ Proto Methods   │
                                                └─────────────────┘
                                                         │
                                                         │ 3. Call Same Service
                                                         ▼
                                                ┌─────────────────┐
                                                │ ProductService  │
                                                │ @Service        │
                                                │ Business Logic  │
                                                └─────────────────┘
                                                         │
                                                         │ 4. Query Database
                                                         ▼
                                                ┌─────────────────┐
                                                │ ProductRepository│
                                                │ @Repository     │
                                                │ JPA/Hibernate   │
                                                └─────────────────┘
                                                         │
                                                         │ 5. Data
                                                         ▼
                                                ┌─────────────────┐
                                                │   H2 Database   │
                                                │   In-Memory     │
                                                └─────────────────┘
```

## **Side-by-Side Comparison**

| Step | HTTP REST Flow | gRPC Flow |
|------|----------------|-----------|
| **1** | HTTP Request → Tomcat | gRPC Request → Netty |
| **2** | JSON Payload | Protobuf Payload |
| **3** | @RestController | @GrpcService |
| **4** | Same @Service Layer | Same @Service Layer |
| **5** | Same Database | Same Database |
| **6** | JSON Response | Protobuf Response |

## **Key Insights**

### 🎯 **Single Application, Dual Interface**
- **One codebase** serving two protocols
- **Shared business logic** and data layer
- **Different presentation layers** for different clients

### 🚀 **Performance Characteristics**
- **HTTP REST**: Human-readable, browser-friendly
- **gRPC**: Binary, high-performance, streaming

### 🔧 **Development Benefits**
- **Code reuse**: Business logic shared
- **Flexibility**: Choose protocol per use case
- **Testing**: Can test same logic via both interfaces

This architecture is perfect for **modern applications** that need to serve both **human users** (web/mobile) and **other services** (microservices architecture)!
