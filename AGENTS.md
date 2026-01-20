# AGENTS.md - Agate Development Guide

This document provides essential information for agentic coding agents working on the Agate authentication server project.

## Project Overview

Agate is a central authentication server for OBiBa applications built with:
 - **Java 21** with Spring Boot 4.0.0
- **Maven** multi-module project
- **MongoDB** for data persistence
- **Apache Shiro** for security
- **Jersey/JAX-RS** for REST API
- **Angular** frontend (TypeScript)

## Module Structure

```
agate/
├── agate-core/          # Core business logic, domain models, services
├── agate-web-model/     # Generated protobuf models and DTOs
├── agate-rest/          # REST API endpoints and mappers
├── agate-ui/            # Angular frontend application
├── agate-webapp/        # Spring Boot web application
└── agate-dist/          # Distribution and packaging
```

## Build and Development Commands

### Core Maven Commands
```bash
# Build entire project
make install          # or: mvn install

# Build without tests
mvn install -Dmaven.test.skip=true

# Clean build
make clean            # or: mvn clean

# Build specific modules
make core             # Build agate-core module
make rest             # Build agate-rest module
make ui               # Build agate-ui module
make webapp           # Build agate-webapp module
```

### Testing Commands
```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl agate-core
mvn test -pl agate-rest

# Run single test class
mvn test -Dtest=UserServiceTest

# Run single test method
mvn test -Dtest=UserServiceTest#testCreateUser

# Run tests with specific profile
mvn test -Pdev
```

### Development Server
```bash
# Start development server (HTTP:8081, HTTPS:8444)
make debug

# Start with production profile
make run-prod

# View logs
make log              # Tail main log
make restlog          # Tail REST log
```

### Other Useful Commands
```bash
# Dependency analysis
make dependencies-tree
make dependencies-update
make plugins-update

# Database operations
make drop-mongo       # Drop MongoDB database
make clear-log        # Clear log files
```

## Code Style Guidelines

### Package Structure
- Base package: `org.obiba.agate`
- Follow standard Maven directory layout
- Domain models in `org.obiba.agate.domain`
- Services in `org.obiba.agate.service`
- Repositories in `org.obiba.agate.repository`
- REST resources in `org.obiba.agate.web.rest`
- Controllers in `org.obiba.agate.web.controller`

### Naming Conventions
- **Classes**: PascalCase (e.g., `UserService`, `UserRepository`)
- **Methods**: camelCase (e.g., `createUser()`, `findByEmail()`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_PAGE_SIZE`)
- **Variables**: camelCase with descriptive names
- **Packages**: lowercase with dots separating words

### Import Organization
1. `java.*` and `jakarta.*` imports
2. Third-party library imports
3. `org.obiba.*` imports
- Use wildcard imports only for constant classes
- Remove unused imports

### Annotations Usage
- `@Inject` for dependency injection
- `@Component` for Spring-managed beans
- `@Service` for service layer classes
- `@Repository` for data access classes
- `@Document` for MongoDB entities
- `@Path`, `@GET`, `@POST` for JAX-RS endpoints
- `@RequiresRoles` for Shiro security
- `@Timed` for metrics collection

### Error Handling
- Create specific exception classes extending `RuntimeException`
- Use standard HTTP status codes in REST responses
- Implement exception mappers for REST layers
- Log errors at appropriate levels
- Return meaningful error messages

### Testing Guidelines
- Test classes end with `Test` suffix
- Use JUnit for unit tests
- Use AssertJ for assertions
- Mock dependencies with EasyMock
- Follow AAA pattern (Arrange, Act, Assert)
- Test both happy path and error scenarios

### Security Patterns
- Use Shiro annotations for method-level security
- Apply principle of least privilege
- Validate all input parameters
- Sanitize output data
- Use HTTPS in production
- Implement proper session management

### Database Patterns
- Use `@Document` annotation for MongoDB entities
- Add `@Indexed` for frequently queried fields
- Extend `AbstractAuditableDocument` for audit trails
- Use repository pattern for data access
- Consider data consistency and transaction boundaries

### API Design
- Use RESTful conventions
- Implement proper HTTP status codes
- Use DTOs for API responses
- Version APIs when breaking changes
- Document endpoints with proper comments
- Handle pagination for large datasets

## Configuration Profiles

### Available Profiles
- `dev` - Development with debug features
- `ci-build` - Continuous integration build
- Default production settings

### Environment Variables
- `AGATE_HOME` - Application home directory
- `AGATE_LOG` - Log file directory

## Key Dependencies

### Core Framework
- Spring Boot 3.5.7
- Spring Data MongoDB
- Apache Shiro 1.13.0
- Jersey 3.1.3

### Utilities
- Google Guava
- Apache Commons Lang
- Joda Time

### Testing
- JUnit
- AssertJ
- EasyMock

## Development Notes

- Java 21 is required (update alternatives if needed)
- Use MongoDB for development and testing
- Frontend builds are handled through Maven plugin
- Follow GPL3 license requirements
- Maintain backward compatibility when possible
- Write comprehensive tests for new features

## Common Patterns

### Service Layer
```java
@Service
public class UserService {
  
  @Inject
  private UserRepository userRepository;
  
  @Timed
  public User createUser(UserDTO dto) {
    validateUserDTO(dto);
    User user = new User();
    // mapping logic
    return userRepository.save(user);
  }
}
```

### REST Resource
```java
@Component
@Path("/users")
@RequiresRoles("agate-administrator")
public class UsersResource {
  
  @Inject
  private UserService userService;
  
  @POST
  @Timed
  public Response createUser(UserDTO dto) {
    // implementation
  }
}
```

### Domain Entity
```java
@Document
public class User extends AbstractAuditableDocument {
  
  @Indexed(unique = true)
  private String name;
  
  @Indexed(unique = true)
  private String email;
  
  // getters and setters
}
```

This guide should help agents understand the project structure, conventions, and common patterns used in Agate development.