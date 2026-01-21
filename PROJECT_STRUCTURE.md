# Project Structure

This document describes the structure and organization of the E-commerce Inventory Management Service codebase.

## Directory Structure

```
inventory-management-service/
├── pom.xml                              # Maven project configuration
├── README.md                            # Project documentation
├── PROJECT_STRUCTURE.md                 # This file
│
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/inventory/
│   │   │   │
│   │   │   ├── InventoryManagementApplication.java  # Main Spring Boot application
│   │   │   │
│   │   │   ├── config/                  # Configuration classes
│   │   │   │   └── OpenApiConfig.java   # Swagger/OpenAPI configuration
│   │   │   │
│   │   │   ├── controller/              # REST API Controllers
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   └── SkuController.java
│   │   │   │
│   │   │   ├── dto/                     # Data Transfer Objects
│   │   │   │   ├── ApiResponse.java     # Generic API response wrapper
│   │   │   │   ├── PagedResponse.java   # Paginated response wrapper
│   │   │   │   ├── CategoryDto.java
│   │   │   │   ├── CreateCategoryRequest.java
│   │   │   │   ├── UpdateCategoryRequest.java
│   │   │   │   ├── ProductDto.java
│   │   │   │   ├── CreateProductRequest.java
│   │   │   │   ├── UpdateProductRequest.java
│   │   │   │   ├── SkuDto.java
│   │   │   │   ├── CreateSkuRequest.java
│   │   │   │   └── UpdateSkuRequest.java
│   │   │   │
│   │   │   ├── entity/                  # JPA Entities
│   │   │   │   ├── Category.java
│   │   │   │   ├── Product.java
│   │   │   │   └── Sku.java
│   │   │   │
│   │   │   ├── exception/               # Custom Exceptions & Handlers
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── InvalidOperationException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │
│   │   │   ├── mapper/                  # MapStruct Mappers
│   │   │   │   ├── CategoryMapper.java
│   │   │   │   ├── ProductMapper.java
│   │   │   │   └── SkuMapper.java
│   │   │   │
│   │   │   ├── repository/              # Spring Data JPA Repositories
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   └── SkuRepository.java
│   │   │   │
│   │   │   └── service/                 # Business Logic Services
│   │   │       ├── CategoryService.java
│   │   │       ├── ProductService.java
│   │   │       └── SkuService.java
│   │   │
│   │   └── resources/
│   │       ├── application.yaml         # Default configuration (H2 database)
│   │       └── application-prod.yaml    # Production configuration (PostgreSQL)
│   │
│   └── test/
│       ├── java/com/ecommerce/inventory/
│       │   │
│       │   ├── InventoryManagementApplicationTests.java  # Context load test
│       │   │
│       │   ├── controller/              # Controller Tests
│       │   │   ├── CategoryControllerTest.java
│       │   │   ├── ProductControllerTest.java
│       │   │   └── SkuControllerTest.java
│       │   │
│       │   ├── repository/              # Repository Tests
│       │   │   ├── CategoryRepositoryTest.java
│       │   │   ├── ProductRepositoryTest.java
│       │   │   └── SkuRepositoryTest.java
│       │   │
│       │   └── service/                 # Service Tests
│       │       ├── CategoryServiceTest.java
│       │       ├── ProductServiceTest.java
│       │       └── SkuServiceTest.java
│       │
│       └── resources/
│           └── application-test.yaml    # Test configuration
│
└── target/                              # Build output directory
    └── site/jacoco/                     # Coverage reports
```

## Architecture Overview

### Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    Controller Layer                          │
│  (REST endpoints, request validation, response formatting)   │
├─────────────────────────────────────────────────────────────┤
│                      DTO Layer                               │
│  (Request/Response objects, API contracts)                   │
├─────────────────────────────────────────────────────────────┤
│                     Service Layer                            │
│  (Business logic, validation rules, transactions)            │
├─────────────────────────────────────────────────────────────┤
│                     Mapper Layer                             │
│  (Entity <-> DTO transformations)                            │
├─────────────────────────────────────────────────────────────┤
│                   Repository Layer                           │
│  (Database operations, JPA queries)                          │
├─────────────────────────────────────────────────────────────┤
│                     Entity Layer                             │
│  (JPA entities, database schema)                             │
└─────────────────────────────────────────────────────────────┘
```

## Package Descriptions

### `config/`
Contains Spring configuration classes.
- **OpenApiConfig.java** - Configures Swagger/OpenAPI documentation settings

### `controller/`
REST API controllers that handle HTTP requests and responses.
- **CategoryController.java** - Endpoints for category CRUD operations
- **ProductController.java** - Endpoints for product CRUD with search/filter/pagination
- **SkuController.java** - Endpoints for SKU management (nested under products)

### `dto/`
Data Transfer Objects for API request and response payloads.
- **Request DTOs** - Contain validation annotations for input data
- **Response DTOs** - Formatted data for API responses
- **ApiResponse** - Standardized wrapper for all API responses
- **PagedResponse** - Wrapper for paginated results

### `entity/`
JPA entity classes representing database tables.
- **Category** - Product groupings (one-to-many with Product)
- **Product** - Core product data (many-to-one with Category, one-to-many with SKU)
- **Sku** - Product variants (many-to-one with Product)

### `exception/`
Custom exceptions and global exception handling.
- **ResourceNotFoundException** - 404 errors for missing resources
- **DuplicateResourceException** - 409 errors for duplicate entries
- **InvalidOperationException** - 400 errors for invalid operations
- **GlobalExceptionHandler** - Centralized exception handling with @RestControllerAdvice

### `mapper/`
MapStruct interfaces for object mapping.
- Handles conversion between entities and DTOs
- Supports partial updates with null-value ignoring

### `repository/`
Spring Data JPA repository interfaces.
- **CategoryRepository** - Category data access with custom finders
- **ProductRepository** - Product queries with filtering and pagination
- **SkuRepository** - SKU queries with product-based lookups

### `service/`
Business logic and transaction management.
- **CategoryService** - Category business rules, validation
- **ProductService** - Product management with category association
- **SkuService** - SKU operations with product validation

## Key Design Decisions

### 1. **Layered Architecture**
Clear separation of concerns with distinct layers for controllers, services, and repositories.

### 2. **DTO Pattern**
Separate DTOs for requests and responses to:
- Decouple API contract from internal entities
- Apply different validations for create vs update operations
- Hide internal entity details from API consumers

### 3. **MapStruct for Mapping**
Using MapStruct for type-safe, compile-time object mapping instead of manual mapping or reflection-based mappers.

### 4. **Global Exception Handling**
Centralized exception handling with `@RestControllerAdvice` provides:
- Consistent error response format
- Clean controller code
- Proper HTTP status codes

### 5. **Validation**
Jakarta Bean Validation annotations on DTOs for:
- Automatic request validation
- Descriptive error messages
- Clean validation logic separation

### 6. **Nested REST Resources**
SKUs are accessed as nested resources under products (`/products/{id}/skus`) to:
- Clearly express the relationship
- Ensure product context in all SKU operations

### 7. **Soft Delete Prevention**
Categories with associated products cannot be deleted (handled in business logic).

## Testing Strategy

### Unit Tests
- Service layer tests with mocked repositories
- Controller tests with MockMvc
- Repository tests with @DataJpaTest

### Test Coverage
- JaCoCo plugin configured for coverage reporting
- Reports generated in `target/site/jacoco/`

## API Response Format

All API responses follow a consistent format:

```json
{
  "success": true/false,
  "message": "Optional message",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00"
}
```

Error responses include validation details when applicable:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "fieldName": "Error message"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

