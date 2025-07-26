# Spring Boot Demo Project

A comprehensive demo project showcasing Spring Boot with H2 Database, gRPC, and REST API integration.

## Features

- **Spring Boot 3.x** - Modern Java framework
- **H2 Database** - In-memory database for development
- **REST API** - Full CRUD operations with Swagger documentation
- **gRPC Services** - High-performance RPC with bidirectional streaming
- **JPA/Hibernate** - Data persistence and ORM
- **Bean Validation** - Input validation
- **Docker Support** - Containerization ready
- **Unit Tests** - Comprehensive test coverage

## Tech Stack

- Java 17
- Spring Boot 3.2.1
- Spring Data JPA
- H2 Database
- gRPC with Protocol Buffers
- Swagger/OpenAPI 3
- Maven
- Docker & Docker Compose
- JUnit 5

## Project Structure

```
src/
├── main/
│   ├── java/com/demo/springboot/
│   │   ├── controller/          # REST API controllers
│   │   ├── entity/              # JPA entities
│   │   ├── grpc/service/        # gRPC service implementations
│   │   ├── repository/          # Data repositories
│   │   └── service/             # Business logic services
│   ├── proto/                   # Protocol Buffer definitions
│   └── resources/
│       ├── application.yml      # Application configuration
│       └── data.sql            # Sample data
└── test/                       # Unit and integration tests
```

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker (optional)

### Running the Application

1. **Clone and build:**
   ```bash
   git clone <repository-url>
   cd springboot-grpc-rest-demo
   mvn clean package -DskipTests
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
   
   Or run the JAR directly:
   ```bash
   java -jar target/springboot-grpc-rest-demo-0.0.1-SNAPSHOT.jar
   ```

3. **Using Docker:**
   ```bash
   mvn clean package -DskipTests
   docker-compose up --build
   ```

### Accessing the Application

- **REST API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **H2 Console:** http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: `password`

**Note:** gRPC services are currently in development. The protocol buffer definitions are included, but the full gRPC server implementation will be completed in a future update.

## API Endpoints

### User Management
- `GET /api/users` - Get all users (paginated)
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `GET /api/users/search?name=` - Search users by name
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Product Management
- `GET /api/products` - Get all products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search?name=` - Search products
- `GET /api/products/price-range?minPrice=&maxPrice=` - Filter by price
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `PATCH /api/products/{id}/stock?quantity=` - Update stock
- `DELETE /api/products/{id}` - Delete product

## gRPC Services

### User Service
- `CreateUser` - Create a new user
- `GetUser` - Get user by ID
- `UpdateUser` - Update user information
- `DeleteUser` - Delete user
- `GetAllUsers` - Get paginated users
- `SearchUsers` - Search users by name
- `StreamUsers` - Server streaming of users
- `CreateUsersBatch` - Client streaming for batch creation
- `ProcessUserStream` - Bidirectional streaming

### Product Service
- Similar operations available for products
- Supports all CRUD operations
- Streaming capabilities for bulk operations

## Database Configuration

The application uses H2 in-memory database with the following default settings:

- **URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** `password`
- **Console:** Available at `/h2-console`

Sample data is automatically loaded from `data.sql` on startup.

## Testing

Run all tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=UserControllerTest
```

## Docker Deployment

1. **Build the image:**
   ```bash
   docker build -t springboot-grpc-rest-demo .
   ```

2. **Run with Docker Compose:**
   ```bash
   docker-compose up -d
   ```

## Configuration

Key configuration properties in `application.yml`:

```yaml
server:
  port: 8080              # REST API port

grpc:
  server:
    port: 9090            # gRPC server port

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: password
  
  h2:
    console:
      enabled: true
      path: /h2-console
```

## Development

### Adding New Entities

1. Create entity class in `entity/` package
2. Create repository interface extending `JpaRepository`
3. Implement service class with business logic
4. Create REST controller for API endpoints
5. Define Protocol Buffer messages in `.proto` files
6. Implement gRPC service
7. Add unit tests

### Protocol Buffers

Proto files are located in `src/main/proto/`. After modifying proto files, run:

```bash
mvn clean compile
```

This will regenerate the Java classes from Protocol Buffer definitions.

## Monitoring and Management

The application includes Spring Boot Actuator for monitoring:

- **Health Check:** `/actuator/health`
- **Metrics:** `/actuator/metrics`
- **Info:** `/actuator/info`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes and add tests
4. Run tests to ensure they pass
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For questions or issues, please create an issue in the repository or contact the development team.
