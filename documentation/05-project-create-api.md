# Project Create API

## Goal

Add the first write operation to the portfolio backend.

The API now allows a client to create a new portfolio project through:

`POST /api/projects`

The implementation includes:

- a dedicated request DTO
- request validation
- service-layer persistence
- duplicate slug detection
- automatic timestamp handling
- structured validation errors
- appropriate HTTP status codes

---

## Request Flow

The project creation flow is:

```text
HTTP POST request
        ↓
JSON request body
        ↓
CreateProjectRequest
        ↓
Jakarta Validation
        ↓
ProjectController
        ↓
ProjectService
        ↓
ProjectRepository
        ↓
Hibernate / JPA
        ↓
PostgreSQL
        ↓
ProjectDetailResponse
        ↓
201 Created
```

This creates the first complete write path through the backend.

---

## Endpoint

### POST `/api/projects`

Creates a new portfolio project.

Example request:

```json
{
  "slug": "personal-portfolio",
  "title": "Personal Portfolio",
  "shortDescription": "A full-stack developer portfolio built with Spring Boot, React, TypeScript and PostgreSQL.",
  "description": "A full-stack personal portfolio with a Spring Boot REST API, PostgreSQL persistence and a React TypeScript frontend. The application will later include AI-powered portfolio exploration.",
  "githubUrl": null,
  "liveUrl": null,
  "imageUrl": null,
  "featured": true,
  "displayOrder": 2
}
```

A successful request returns:

`201 Created`

with the created project in the response body.

The response also contains a `Location` header pointing to the project's detail endpoint:

```text
/api/projects/personal-portfolio
```

---

## Create Request DTO

Incoming project creation data is represented by:

`CreateProjectRequest`

The API does not accept the JPA `Project` entity directly.

This creates a clear separation:

```text
Client input
     ↓
CreateProjectRequest
     ↓
Service
     ↓
Project entity
     ↓
Database
```

The request DTO contains only fields the client is allowed to provide.

The client does not control fields such as:

- database ID
- creation timestamp
- update timestamp

These remain server-managed values.

---

## Validation

Jakarta Validation is used to reject invalid input before it reaches the service layer.

Examples include:

```java
@NotBlank
@Size(max = 150)
String slug
```

and:

```java
@Min(0)
int displayOrder
```

Validation rules currently include:

| Field | Validation |
|---|---|
| `slug` | Required, maximum 150 characters |
| `title` | Required, maximum 200 characters |
| `shortDescription` | Required, maximum 500 characters |
| `description` | Required |
| `githubUrl` | Optional, maximum 500 characters |
| `liveUrl` | Optional, maximum 500 characters |
| `imageUrl` | Optional, maximum 500 characters |
| `displayOrder` | Must be zero or greater |

The controller activates validation using:

```java
@Valid @RequestBody CreateProjectRequest request
```

This means invalid requests are rejected before the service attempts to persist them.

---

## Validation at Multiple Layers

Some rules exist both in the Java application and in PostgreSQL.

For example:

```text
Java validation:
displayOrder >= 0

Database constraint:
CHECK (display_order >= 0)
```

This duplication is intentional.

The application layer provides useful feedback to API clients.

The database layer provides final data-integrity protection regardless of how the database is accessed.

---

## Service-Layer Creation

Project creation is handled by `ProjectService`.

The service:

1. checks whether the slug already exists
2. creates a new `Project` entity
3. copies the permitted request values into the entity
4. saves the entity using `ProjectRepository`
5. maps the saved entity into `ProjectDetailResponse`

The method uses:

```java
@Transactional
```

because it modifies database state.

Read-only service methods use:

```java
@Transactional(readOnly = true)
```

instead.

---

## Duplicate Slug Handling

Project slugs are unique.

Before saving a project, the service checks:

```java
projectRepository.existsBySlug(request.slug())
```

If the slug already exists, the service throws:

`DuplicateProjectSlugException`

The exception is handled globally and returned as:

`409 Conflict`

Example response:

```json
{
  "error": "duplicate_project_slug",
  "message": "A project with slug 'personal-portfolio' already exists."
}
```

The application-level check provides a meaningful API response.

The PostgreSQL `UNIQUE` constraint on `slug` still remains the final integrity guarantee.

This is important because an application-level existence check alone cannot completely prevent two concurrent requests from attempting to insert the same slug.

---

## Global Exception Handling

REST errors are handled centrally through:

`GlobalExceptionHandler`

The class uses:

```java
@RestControllerAdvice
```

This allows exception handling to remain separate from individual controller methods.

The controller therefore does not need repetitive `try/catch` logic.

The current error flow is:

```text
Application exception
        ↓
GlobalExceptionHandler
        ↓
HTTP status + structured JSON response
```

---

## Validation Error Handling

Invalid request bodies produce:

`MethodArgumentNotValidException`

The global exception handler converts the individual field errors into a structured response.

Example:

```json
{
  "error": "validation_failed",
  "fields": {
    "slug": "must not be blank",
    "title": "must not be blank",
    "shortDescription": "must not be blank",
    "description": "must not be blank",
    "displayOrder": "must be greater than or equal to 0"
  }
}
```

The HTTP status is:

`400 Bad Request`

This is more useful to API clients than exposing Spring's internal validation exception structure.

---

## Timestamp Management

The `Project` entity uses JPA lifecycle callbacks to maintain timestamps.

Before an entity is inserted:

```java
@PrePersist
protected void onCreate()
```

sets:

```text
createdAt
updatedAt
```

to the current time.

Before an existing entity is updated:

```java
@PreUpdate
protected void onUpdate()
```

updates:

```text
updatedAt
```

This makes timestamp behavior explicit at the application level.

The database still contains default timestamp values as additional protection.

---

## HTTP Status Codes

The create endpoint currently uses the following responses:

| Situation | HTTP Status |
|---|---|
| Project created successfully | `201 Created` |
| Invalid request data | `400 Bad Request` |
| Duplicate project slug | `409 Conflict` |

These responses represent different categories of failure.

`400 Bad Request` means the submitted data does not satisfy the request requirements.

`409 Conflict` means the request itself is valid but conflicts with the current state of the application because the slug already exists.

---

## Location Header

A successful project creation returns a `Location` header.

For example:

```text
Location: /api/projects/personal-portfolio
```

This identifies the API resource representing the newly created project.

The controller constructs this location from the project's slug and returns it with:

```java
ResponseEntity.created(location)
```

---

## DTO Direction

The create API now uses different DTOs for different responsibilities:

```text
CreateProjectRequest
        ↓
incoming client data

ProjectSummaryResponse
        ↓
project list response

ProjectDetailResponse
        ↓
single project / created project response
```

This avoids using one large DTO for unrelated API operations.

It also allows each API contract to evolve independently.

---

## Manual API Verification

The endpoint was tested using Postman.

Three cases were verified.

### Valid Request

Expected:

`201 Created`

Result:

Successful.

### Duplicate Slug

The same slug was submitted more than once.

Expected:

`409 Conflict`

Result:

Successful.

### Invalid Request

Required string fields were submitted as blank values and `displayOrder` was negative.

Expected:

`400 Bad Request`

Result:

Successful.

After adding custom validation error handling, invalid requests returned a structured field-error response.

---

## Current Project API

The backend now supports:

```text
GET  /api/projects
GET  /api/projects/{slug}
POST /api/projects
```

The application therefore supports both read and create operations for the `Project` domain.

---

> **Architectural principle:** API input, persistence models, and API output should remain separate concerns. Validation should happen close to the API boundary, business rules should live in the service layer, and the database should remain the final authority for data integrity.