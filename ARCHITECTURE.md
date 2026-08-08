# ExcelKidsHub Platform - Architecture Documentation

## Overview

ExcelKidsHub Platform is a Spring Boot-based backend API that powers multiple applications:
- **excelkidshub.in** - Marketing Website
- **read.excelkidshub.in** - Interactive Reading Studio
- **app.excelkidshub.in** - Student Dashboard

All applications share a single authentication system and database.

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.0**
- **Maven**
- **PostgreSQL** (Neon in production)
- **Spring Security** with JWT
- **Spring Data JPA**
- **OpenAPI (Swagger)** for API documentation
- **Lombok** for boilerplate reduction

## Package Structure

The project follows a **feature-based architecture** instead of a traditional layered architecture.

```
in.excelkidshub.platform
├── ExcelKidsHubPlatformApplication.java    # Main application class
│
├── common/                                  # Shared components
│   ├── config/                             # Configuration classes
│   │   ├── AppConfig.java                  # Application properties
│   │   ├── CorsConfig.java                 # CORS configuration
│   │   └── JpaConfig.java                  # JPA auditing configuration
│   │
│   ├── controller/                         # Common controllers
│   │   └── HealthController.java           # Health check endpoint
│   │
│   ├── dto/                                # Common DTOs
│   │   ├── ApiResponse.java                # Standard API response wrapper
│   │   └── PageResponse.java               # Paginated response wrapper
│   │
│   ├── entity/                             # Common entities
│   │   └── BaseEntity.java                 # Base entity with audit fields
│   │
│   ├── exception/                          # Custom exceptions
│   │   ├── ResourceNotFoundException.java
│   │   ├── BadRequestException.java
│   │   ├── UnauthorizedException.java
│   │   ├── ConflictException.java
│   │   └── GlobalExceptionHandler.java     # Centralized exception handling
│   │
│   ├── security/                           # Security configuration
│   │   ├── SecurityConfig.java             # Spring Security configuration
│   │   ├── JwtAuthenticationFilter.java    # JWT filter
│   │   └── JwtProperties.java              # JWT configuration properties
│   │
│   ├── constants/                          # Application constants
│   │   ├── ApiConstants.java               # API-related constants
│   │   ├── SecurityConstants.java          # Security-related constants
│   │   ├── ErrorCodes.java                 # Standardized error codes
│   │   └── AppConstants.java               # General application constants
│   │
│   └── util/                               # Utility classes
│       ├── DateTimeUtil.java                # Date/time operations
│       ├── ValidationUtil.java             # Validation utilities
│       └── StringUtil.java                 # String operations
│
├── auth/                                   # Authentication module (future)
├── user/                                   # User management (future)
├── course/                                 # Course management (future)
├── subscription/                           # Subscription management (future)
├── payment/                                # Payment processing (future)
├── reading/                                # Reading studio (future)
├── progress/                               # Progress tracking (future)
├── dashboard/                              # Dashboard features (future)
└── admin/                                  # Admin features (future)
```

## Module Responsibilities

### Common Module
- **config**: Application configuration, CORS, JPA settings
- **controller**: Shared controllers (health check, etc.)
- **dto**: Reusable data transfer objects
- **entity**: Base entity class with audit fields
- **exception**: Custom exceptions and global exception handling
- **security**: Security configuration and JWT infrastructure
- **constants**: Application-wide constants to avoid hardcoding
- **util**: Reusable utility methods

### Feature Modules (Future)
Each feature module will contain:
- **controller**: REST endpoints
- **service**: Business logic
- **repository**: Data access
- **dto**: Feature-specific DTOs
- **entity**: Feature-specific entities
- **mapper**: DTO-Entity mapping
- **exception**: Feature-specific exceptions

## API Conventions

### Response Format

All API endpoints return a standardized response:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "error": null,
  "timestamp": "2024-01-01T12:00:00"
}
```

### Error Response Format

```json
{
  "success": false,
  "message": "Operation failed",
  "data": null,
  "error": "Error details",
  "timestamp": "2024-01-01T12:00:00"
}
```

### Endpoint Conventions

- **Base Path**: `/api`
- **Health Check**: `/api/health`
- **Authentication**: `/api/auth/**`
- **Public Endpoints**: `/api/public/**`
- **Swagger UI**: `/swagger-ui.html`

### HTTP Methods

- **GET**: Retrieve resources
- **POST**: Create resources
- **PUT**: Update resources (full update)
- **PATCH**: Partial updates
- **DELETE**: Remove resources

### Pagination

Paginated endpoints use:
- `page`: Page number (default: 0)
- `size`: Page size (default: 10, max: 100)
- `sort`: Sort field and direction

## Database Design Principles

### ID Strategy

- **Use Long IDs** for all entities (not UUID)
- **Rationale**: Simpler, faster joins, easier debugging
- **Public APIs** can expose random tokens later if needed

### Entity Design

- **Generic Concepts**: Use Course, Book, Plan, Subscription, Progress, User
- **No Hardcoded Levels**: Never hardcode Level-1, Level-2, etc.
- **Database-Driven**: Future levels should be insertable through database only
- **Audit Fields**: All entities extend BaseEntity with:
  - `id` (Long, auto-generated)
  - `createdAt` (LocalDateTime)
  - `updatedAt` (LocalDateTime)
  - `createdBy` (String)
  - `updatedBy` (String)
  - `active` (Boolean, default true)

### Naming Conventions

- **Tables**: snake_case (e.g., `user_profiles`, `course_plans`)
- **Columns**: snake_case (e.g., `created_at`, `updated_at`)
- **Indexes**: `idx_table_name_column_name`
- **Foreign Keys**: `fk_table_name_reference_table`

### Schema Management

- **Development**: `ddl-auto: update` (temporary)
- **Production**: `ddl-auto: validate` with Flyway migrations
- **Plan**: Switch to Flyway before production deployment

## Coding Standards

### General Principles

- **SOLID Principles**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **Clean Architecture**: Practical implementation where possible
- **DRY**: Don't Repeat Yourself
- **KISS**: Keep It Simple, Stupid

### Dependency Injection

- **Use Constructor Injection** (not field injection)
- **Lombok @RequiredArgsConstructor** for constructors

### Code Organization

- **Controllers**: Thin - only handle HTTP concerns
- **Services**: Business logic belongs here
- **Repositories**: Data access only
- **DTOs**: API contracts - validation here
- **Mappers**: DTO-Entity conversions

### Code Quality

- **Lombok**: Reduce boilerplate code
- **Validation**: Use Jakarta Validation annotations
- **Logging**: Use `@Slf4j` (not System.out.println)
- **Documentation**: Document public classes and methods
- **Meaningful Names**: Use descriptive variable and method names

### Class Design

- **Small Classes**: Keep classes focused and maintainable
- **Single Responsibility**: Each class should have one reason to change
- **Avoid Premature Optimization**: Optimize when necessary, not prematurely

## Security Principles

### Authentication

- **JWT-based**: Stateless token authentication
- **Future SSO**: Support Single Sign-On across subdomains
- **Protected Endpoints**: All protected endpoints validate JWT
- **No OAuth Yet**: OAuth login will be implemented later

### Authorization

- **Role-Based**: Use roles (ADMIN, TEACHER, PARENT, STUDENT)
- **Method Security**: Use `@PreAuthorize` annotations
- **Resource-Level**: Fine-grained permissions where needed

### Security Configuration

- **Password Encoding**: BCrypt
- **CORS**: Configured for frontend domains
- **CSRF**: Disabled for stateless API
- **Session Management**: Stateless (JWT)

## Development Roadmap

### Phase 1: Foundation ✅
- Project structure
- Base configuration
- Global exception handling
- API response wrapper
- Basic security configuration
- Common utilities
- Health controller
- OpenAPI integration
- Constants package
- BaseEntity

### Phase 2: Database Entities (Next)
- Role entity
- User entity
- Plan entity
- Course entity
- PlanCourse entity
- Subscription entity
- Book entity
- BookPage entity
- Payment entity
- StudentProgress entity
- Review relationships

### Phase 3: Authentication
- UserDetailsService implementation
- JwtUtil implementation
- Authentication controller
- Login/Registration endpoints
- JWT token generation/validation
- Password reset flow

### Phase 4: Plans
- Plan CRUD operations
- Plan features
- Plan pricing
- Plan validation

### Phase 5: Courses
- Course CRUD operations
- Course content management
- Course-Plan associations
- Course enrollment

### Phase 6: Books
- Book CRUD operations
- Book page management
- Book content structure
- Book metadata

### Phase 7: Reading APIs
- Reading progress tracking
- Book page navigation
- Reading statistics
- Interactive features

### Phase 8: Progress APIs
- Student progress tracking
- Course completion
- Achievement tracking
- Progress reports

### Phase 9: Razorpay
- Payment gateway integration
- Subscription payments
- Payment verification
- Refund handling

### Phase 10: Admin APIs
- Admin dashboard
- User management
- Content moderation
- Analytics and reports

### Phase 11: Parent Portal
- Parent dashboard
- Child progress monitoring
- Subscription management
- Communication features

### Phase 12: Teacher Portal
- Teacher dashboard
- Student management
- Content creation
- Assessment tools

## Configuration

### Profiles

- **Default**: Base configuration
- **Dev**: Development environment (`application-dev.yml`)
- **Prod**: Production environment (`application-prod.yml`)
- **Test**: Test environment (`application-test.yml`)

### Environment Variables

- `DATABASE_URL`: PostgreSQL connection URL
- `DATABASE_USERNAME`: Database username
- `DATABASE_PASSWORD`: Database password
- `JWT_SECRET`: JWT signing secret
- `JWT_EXPIRATION`: JWT token expiration (ms)
- `MAIL_HOST`: SMTP server host
- `MAIL_PORT`: SMTP server port
- `MAIL_USERNAME`: SMTP username
- `MAIL_PASSWORD`: SMTP password

## API Documentation

### Swagger UI

Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

### OpenAPI Specification

OpenAPI spec available at: `http://localhost:8080/v3/api-docs`

## Logging

### Logging Levels

- **INFO**: Production default
- **DEBUG**: Development environment
- **ERROR**: Always logged

### Logging Pattern

```
%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

### Logging Best Practices

- Use `@Slf4j` annotation
- Log important business events
- Log exceptions with stack traces
- Avoid excessive logging in production

## Testing

### Test Structure

```
src/test/java/in/excelkidshub/platform
├── common/
│   ├── util/
│   └── exception/
└── [feature modules]
```

### Testing Guidelines

- Unit tests for services
- Integration tests for repositories
- Controller tests for endpoints
- Security tests for protected endpoints

## Deployment

### Health Check

Health check endpoint: `GET /api/health`

Response:
```json
{
  "success": true,
  "message": "Service is healthy",
  "data": {
    "status": "UP"
  }
}
```

### Oracle Cloud

Health check endpoint can be used by Kubernetes and Oracle Cloud for monitoring and auto-scaling.

## Future Integrations

- **Razorpay**: Payment processing
- **Email Services**: Transactional emails
- **AI Pronunciation**: AI-powered pronunciation assessment
- **Mobile App**: Native mobile applications
- **Analytics**: Advanced analytics and reporting

## Maintenance

This architecture document should be updated as the application evolves. It serves as a reference for both developers and AI assistants to maintain consistency across the codebase.

---

**Last Updated**: July 19, 2026
**Version**: 1.0.0
