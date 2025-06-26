# Digital Wallet Service

A Spring Boot service that manages users' money with operations for deposit, withdrawal, and fund transfers between users.

## Requirements

- Java 21
- Maven 3.8.x
- PostgreSQL 14+

## Setup

### Option 1: Local Execution

1. Clone the repository
2. Create a PostgreSQL database named `wallet_service`
3. Update the database credentials in `src/main/resources/application.properties` if necessary
4. Compile and run the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### Option 2: Docker Execution (Recommended)

1. Clone the repository
2. Run the Docker Compose command to start the application and database:
   ```bash
   docker-compose up -d
   ```

This will start both the Spring Boot application and the PostgreSQL database in Docker containers. The application will be available at http://localhost:8080.

To stop the containers:
   ```bash
   docker-compose down
   ```

To view the application logs:
   ```bash
   docker-compose logs -f app
   ```

**Note**: PostgreSQL data is persisted in a Docker volume, so it won't be lost when the containers are stopped.

## API Documentation

The API is documented using Swagger/OpenAPI. After starting the application, you can access:

- **OpenAPI Documentation**: http://localhost:8080/api-docs
- **Swagger UI Interface**: http://localhost:8080/swagger-ui.html

The Swagger interface allows you to test all endpoints directly in the browser.

## API Endpoints

### Authentication Endpoints

The following endpoints are available for authentication:

```
POST /api/auth/signup
Body:
{
  "username": "string",
  "email": "string",
  "password": "string",
  "roles": ["USER", "ADMIN"]
}
```

```
POST /api/auth/signin
Body:
{
  "username": "string",
  "password": "string"
}
```

### Wallet Endpoints

All wallet endpoints are available at `/api/wallets` and require authentication with a JWT token in the Authorization header.

### Create Wallet
```
POST /api/wallets
Headers:
  Authorization: Bearer {jwt_token}
Body:
{
  "idUsuario": "string"
}
```

### Deposit
```
POST /api/wallets/deposit
Headers:
  Authorization: Bearer {jwt_token}
Body:
{
  "idUsuario": "string",
  "valor": number
}
```

### Withdraw
```
POST /api/wallets/withdraw
Headers:
  Authorization: Bearer {jwt_token}
Body:
{
  "idUsuario": "string",
  "valor": number
}
```

### Transfer
```
POST /api/wallets/transfer
Headers:
  Authorization: Bearer {jwt_token}
Body:
{
  "idUsuarioOrigem": "string",
  "idUsuarioDestino": "string",
  "valor": number
}
```

### Get Balance
```
GET /api/wallets/{idUsuario}/balance
Headers:
  Authorization: Bearer {jwt_token}
```

### Get Historical Balance
```
GET /api/wallets/{idUsuario}/balance/historical?dateTime=2025-06-25T12:00:00
Headers:
  Authorization: Bearer {jwt_token}
```

## System Architecture

### Overview
The system follows a layered architecture with a focus on separation of concerns and testability. The main layers are:

1. **Presentation Layer (Controller)**
   - `WalletController`: Manages HTTP requests
   - Maps REST endpoints to service operations
   - Validates parameters and returns HTTP responses

2. **Service Layer (Service)**
   - `WalletService`: Contains business logic
   - Implements business rules and validations
   - Coordinates operations between layers
   - Ensures operation atomicity through transactions

3. **Repository Layer (Repository)**
   - `WalletRepository`: Interface for wallet operations
   - `TransactionRepository`: Interface for transaction operations
   - Uses JPA/Hibernate for persistence

4. **Domain Layer (Domain)**
   - `Wallet`: Entity representing a wallet
   - `Transaction`: Entity representing a transaction
   - DTOs for data transfer between layers

### Patterns and Practices

1. **Dependency Injection**
   - Using Spring for dependency management
   - Facilitates testability and maintenance

2. **DTOs (Data Transfer Objects)**
   - Clear separation between domain objects and transfer objects
   - Validation with Jakarta Bean Validation annotations

3. **Custom Exceptions**
   - `WalletNotFoundException`
   - `InsufficientFundsException`
   - `WalletAlreadyExistsException`
   - Global exception handling with `GlobalExceptionHandler`

4. **ACID Transactions**
   - All operations are wrapped in transactions
   - Data consistency guarantee
   - Automatic rollback in case of errors

### Technologies and Frameworks

1. **Backend**
   - Java 21
   - Spring Boot 3.3.0
   - Spring Data JPA
   - Hibernate
   - Swagger/OpenAPI for documentation
   - Redis for frequent query caching
   - Spring Security with JWT authentication
   - Resilience4j for Circuit Breaker pattern and fault tolerance

2. **Database**
   - PostgreSQL for production
   - H2 Database for tests
   - JPA/Hibernate for object-relational mapping
   - Redis for cache storage

3. **Testing**
   - JUnit Jupiter
   - Mockito
   - Spring Boot Test
   - In-memory H2 database for integration tests
   - SimpleWalletServiceTest for core functionality testing

## Design Decisions

1. **Database**: PostgreSQL was chosen for its ACID support and transaction robustness.
2. **Currency**: Currently supports Brazilian Real (BRL) as the default currency.
3. **Transactions**: All operations are atomic and tracked in the transactions table for auditing.
4. **Validation**: Complete implementation of validations for values and balances.
5. **Error Handling**: Clear error messages are returned for invalid operations.
6. **Documentation**: API fully documented with Swagger/OpenAPI.
7. **Redis Cache**: Implemented to improve performance of frequent queries.
8. **Security**: JWT-based authentication and authorization with Spring Security.
9. **Resilience**: Circuit Breaker pattern implemented with Resilience4j for fault tolerance.

## Security System

The service implements JWT-based authentication and authorization using Spring Security:

### Main security features:

1. **Authentication endpoints:**
   - `/api/auth/signup`: Register new users
   - `/api/auth/signin`: Authenticate users and receive JWT token

2. **JWT Token:**
   - Stateless authentication using JSON Web Tokens
   - Configurable expiration time (default: 24 hours)
   - Role-based authorization support

3. **Security configuration:**
   - CSRF protection disabled for REST API
   - Public endpoints for authentication and documentation
   - Protected endpoints requiring authentication
   - Password encryption using BCrypt

4. **User management:**
   - User entity with roles
   - Email and username validation
   - Role-based access control

## Resilience System

The service implements the Circuit Breaker pattern using Resilience4j to improve fault tolerance:

### Main resilience features:

1. **Circuit Breaker:**
   - Prevents cascading failures
   - Configurable failure threshold (default: 50%)
   - Automatic transition from open to half-open state
   - Health monitoring through Actuator

2. **Fallback mechanisms:**
   - Graceful degradation for critical operations
   - Default responses when services are unavailable
   - Detailed error logging

3. **Timeout handling:**
   - Configurable timeouts for operations
   - Cancellation of running futures

4. **Retry mechanism:**
   - Automatic retry for transient failures
   - Exponential backoff strategy
   - Configurable maximum attempts

## Redis Cache System

The service implements a cache system using Redis to improve the performance of frequent queries:

### Main cache features:

1. **Configured caches:**
   - `walletBalances`: Stores balance query results (TTL: 5 minutes)
   - `userWallets`: Stores user wallets (TTL: 1 hour)

2. **Invalidation strategies:**
   - Automatic invalidation after write operations (deposit, withdrawal, transfer)
   - Complete cache invalidation after transfers (affects multiple wallets)
   - Configurable TTL to prevent stale data

3. **Benefits:**
   - Significant reduction in database queries
   - Improved latency for frequent operations like balance queries
   - Greater scalability in high concurrency scenarios
   - Reduced load on the PostgreSQL database

4. **Configuration:**
   - Easily configurable via environment variables
   - Integrated with Docker Compose for local development
   - Redis data persistence configured to prevent loss during restarts

## Trade-offs

1. Support for a single currency (BRL) - Can be extended to support multiple currencies.
2. Simple transaction model - Can be enhanced with more complex transaction types.
3. Basic validation - Can be improved with more sophisticated business rules.
4. Eventual cache consistency - In distributed systems, there may be a small delay in update propagation.
5. JWT token storage - Currently no token blacklist for revocation.
6. Circuit breaker configuration - Parameters may need tuning based on production load.
