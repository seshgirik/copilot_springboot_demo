<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

# Spring Boot gRPC REST Demo - Copilot Instructions

This is a comprehensive Spring Boot demo project featuring:
- **Spring Boot 3.x** with Java 17
- **H2 Database** for data persistence
- **REST API** with full CRUD operations
- **gRPC Services** with bidirectional streaming
- **Protocol Buffers** for service definitions
- **JPA/Hibernate** for database operations
- **Bean Validation** for input validation
- **Swagger/OpenAPI** for API documentation
- **Docker** containerization support
- **Unit Tests** with JUnit 5 and MockMvc

## Project Guidelines

### Code Style
- Use Java 17+ features where appropriate
- Follow Spring Boot best practices
- Implement proper error handling
- Add comprehensive JavaDoc comments
- Use meaningful variable and method names

### Entity Design
- JPA entities are in `com.demo.springboot.entity` package
- Use appropriate validation annotations
- Implement proper toString, equals, and hashCode methods
- Follow naming conventions for database tables and columns

### Service Layer
- Business logic resides in service classes
- Handle exceptions appropriately
- Use transaction management where needed
- Implement proper logging

### REST API Design
- Follow RESTful conventions
- Use appropriate HTTP status codes
- Implement pagination for list endpoints
- Add comprehensive Swagger documentation
- Validate input data using Bean Validation

### gRPC Services
- Protocol Buffer definitions are in `src/main/proto/`
- Implement all CRUD operations
- Include streaming examples (server, client, bidirectional)
- Handle errors gracefully with proper gRPC status codes
- Use appropriate message types for requests and responses

### Testing
- Write unit tests for all service methods
- Create integration tests for REST endpoints
- Mock external dependencies
- Achieve good test coverage
- Use meaningful test names and assertions

### Database
- Use H2 in-memory database for development
- Include sample data in `data.sql`
- Design proper relationships between entities
- Use appropriate indexing strategies

### Configuration
- Externalize configuration in `application.yml`
- Support different profiles (dev, test, prod)
- Include Docker configuration
- Document all configuration properties

When working with this project, always consider:
1. Maintainability and readability
2. Performance implications
3. Security best practices
4. Error handling and logging
5. Test coverage and quality
6. Documentation completeness
