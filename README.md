# ExcelKidsHub Platform

Backend API for ExcelKidsHub Digital Learning Platform.

## Technology Stack

- Java 21
- Spring Boot 3.2.0
- Maven
- PostgreSQL
- Spring Security
- JWT Authentication
- Spring Data JPA

## Project Structure

The project follows a feature-based architecture:

```
in.excelkidshub.platform
├── common/              # Shared utilities and configurations
│   ├── config/         # Configuration classes
│   ├── dto/            # Common DTOs
│   ├── exception/      # Custom exceptions
│   ├── security/       # Security configuration
│   └── util/           # Utility classes
├── auth/               # Authentication module
├── user/               # User management
├── course/             # Course management
├── subscription/       # Subscription management
├── payment/            # Payment processing
├── reading/            # Reading studio
├── progress/           # Progress tracking
├── dashboard/          # Dashboard features
└── admin/              # Admin features
```

## Getting Started

### Prerequisites

- Java 21
- Maven 3.8+
- PostgreSQL 14+

### Configuration

Copy `application.yml` and configure the following:

- Database connection
- JWT secret key
- Mail server settings

### Running the Application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api`

## Development

### Profile Configuration

- `application-dev.yml` - Development environment
- `application-prod.yml` - Production environment

### Coding Standards

- Follow SOLID principles
- Use constructor injection
- Keep controllers thin
- Business logic in services
- Use DTOs for API contracts
- Global exception handling

## Future Integrations

- Razorpay payments
- Email services
- Parent Portal
- Teacher Portal
- AI Pronunciation
- Mobile App
