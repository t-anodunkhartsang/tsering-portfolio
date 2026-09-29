# Project Update and Delete API

## Goal

Complete CRUD support for the `Project` domain by adding update and delete operations.

The backend now supports:

```text
CREATE
POST /api/projects

READ
GET /api/projects
GET /api/projects/{slug}

UPDATE
PUT /api/projects/{slug}

DELETE
DELETE /api/projects/{slug}
```

This milestone also introduces consistent handling for missing projects and update-time slug conflicts.

---

## Update Endpoint

### PUT `/api/projects/{slug}`

Updates an existing portfolio project.

The slug in the URL identifies the project that currently exists.

For example:

```text
PUT /api/projects/personal-portfolio
```

The request body represents the desired new state of the project.

Example:

```json
{
  "slug": "personal-portfolio",
  "title": "Personal Developer Portfolio",
  "shortDescription": "A full-stack developer portfolio built with Spring Boot, React, TypeScript and PostgreSQL.",
  "description": "A full-stack personal portfolio demonstrating backend architecture, REST APIs, relational persistence and eventually AI-powered portfolio exploration.",
  "githubUrl": null,
  "liveUrl": null,
  "imageUrl": null,
  "featured": true,
  "displayOrder": 2
}
```

A successful update returns:

`200 OK`

with the updated `ProjectDetailResponse`.

---

## Update Request DTO

Update input is represented by:

`UpdateProjectRequest`

Although its fields currently resemble `CreateProjectRequest`, the two DTOs remain separate.

They represent different API operations:

```text
CreateProjectRequest
        ↓
create a new resource

UpdateProjectRequest
        ↓
replace/update an existing resource
```

Keeping them separate allows the contracts to evolve independently later.

For example, future creation and update requirements may differ.

---

## Update Validation

`UpdateProjectRequest` uses the same validation principles as the create request.

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

Invalid update requests are rejected with:

`400 Bad Request`

and use the same structured validation-error format introduced for project creation.

---

## Project Lookup

Project lookup is centralized in the service layer using a helper method.

Conceptually:

```text
slug
 ↓
ProjectRepository.findBySlug(...)
 ↓
project found
    → continue

project missing
    → ProjectNotFoundException
```

This avoids repeating missing-project handling across multiple service methods.

---

## ProjectNotFoundException

Missing projects are represented by:

`ProjectNotFoundException`

Example:

```java
public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String slug) {
        super("Project with slug '" + slug + "' was not found.");
    }
}
```

The exception is handled centrally by `GlobalExceptionHandler`.

The API returns:

`404 Not Found`

with a structured response such as:

```json
{
  "error": "project_not_found",
  "message": "Project with slug 'does-not-exist' was not found."
}
```

This handling is used consistently by:

```text
GET /api/projects/{slug}
PUT /api/projects/{slug}
DELETE /api/projects/{slug}
```

---

## GET Endpoint Refactoring

Before this milestone, the GET-by-slug endpoint handled an empty result directly in the controller.

The service previously returned:

```java
Optional<ProjectDetailResponse>
```

and the controller converted an empty `Optional` into `404 Not Found`.

The logic was refactored so that the service now throws:

`ProjectNotFoundException`

when a project cannot be found.

This simplifies the controller and centralizes error handling.

The request flow becomes:

```text
GET /api/projects/{slug}
        ↓
ProjectController
        ↓
ProjectService
        ↓
ProjectRepository
        ↓
project missing
        ↓
ProjectNotFoundException
        ↓
GlobalExceptionHandler
        ↓
404 Not Found
```

---

## Service-Layer Update

Project updates are handled inside a transactional service method.

The service:

1. retrieves the existing project by its current slug
2. verifies that the requested slug does not belong to another project
3. applies the request fields to the entity
4. persists the updated entity
5. maps the entity to `ProjectDetailResponse`

The update method uses:

```java
@Transactional
```

because it modifies persistent state.

---

## Slug Changes

The update API allows a project's slug to change.

For example, an existing resource may be addressed as:

```text
/api/projects/personal-portfolio
```

while the update body contains:

```json
{
  "slug": "developer-portfolio"
}
```

The URL parameter identifies the existing resource:

```text
current slug:
personal-portfolio
```

The request body defines the desired new state:

```text
new slug:
developer-portfolio
```

After the update:

```text
/api/projects/personal-portfolio
→ no longer exists

/api/projects/developer-portfolio
→ identifies the updated project
```

Because the slug forms part of the public project URL, the update response includes a `Location` header containing the current resource location.

---

## Duplicate Slug Handling During Update

A simple slug-existence check is not sufficient when updating a project.

Consider the existing project:

```text
id:   2
slug: personal-portfolio
```

If an update keeps the same slug, this check:

```java
existsBySlug("personal-portfolio")
```

would return `true`.

However, that slug belongs to the same project and should not be treated as a conflict.

The repository therefore uses:

```java
boolean existsBySlugAndIdNot(String slug, Long id);
```

Conceptually, this asks:

```text
Does another project,
with a different ID,
already use this slug?
```

Spring Data JPA derives the query from the repository method name.

If another project already owns the requested slug, the API returns:

`409 Conflict`

using the existing `DuplicateProjectSlugException`.

---

## Update Timestamp

The service does not manually set `updatedAt`.

The `Project` entity already contains:

```java
@PreUpdate
protected void onUpdate() {
    updatedAt = OffsetDateTime.now();
}
```

Before Hibernate updates an existing entity, this lifecycle callback updates the timestamp.

The flow is:

```text
Project fields changed
        ↓
Hibernate detects modified entity
        ↓
@PreUpdate
        ↓
updatedAt refreshed
        ↓
SQL UPDATE
```

---

## Delete Endpoint

### DELETE `/api/projects/{slug}`

Deletes an existing portfolio project.

Example:

```text
DELETE /api/projects/developer-portfolio
```

The service first retrieves the entity.

If the project exists:

```text
ProjectRepository.delete(...)
```

is called.

The endpoint then returns:

`204 No Content`

A successful delete intentionally returns no response body.

---

## Why `204 No Content`?

The deleted resource no longer has a representation that needs to be returned.

Therefore:

```text
DELETE successful
        ↓
204 No Content
```

is sufficient.

The API does not return an unnecessary body such as:

```json
{
  "deleted": true
}
```

---

## Delete Missing Resource

Deleting a project that does not exist produces:

`ProjectNotFoundException`

and therefore returns:

`404 Not Found`

Example:

```json
{
  "error": "project_not_found",
  "message": "Project with slug 'developer-portfolio' was not found."
}
```

This gives delete operations the same missing-resource behavior as GET and PUT.

---

## Controller Responsibilities

The controller is intentionally kept thin.

Its responsibilities include:

- receiving HTTP requests
- extracting path variables
- converting JSON request bodies into DTOs
- activating validation
- selecting appropriate HTTP responses

Business logic remains in `ProjectService`.

The intended layering remains:

```text
HTTP
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
JPA / Hibernate
 ↓
PostgreSQL
```

---

## HTTP Status Codes

The Project API now uses the following status codes:

| Situation | HTTP Status |
|---|---|
| Successful GET | `200 OK` |
| Successful creation | `201 Created` |
| Successful update | `200 OK` |
| Successful delete | `204 No Content` |
| Invalid request | `400 Bad Request` |
| Project not found | `404 Not Found` |
| Duplicate slug | `409 Conflict` |

Each status represents a distinct API outcome.

---

## Manual API Verification

The update and delete endpoints were tested using Postman.

The following cases were verified.

### Update Existing Project

```text
PUT /api/projects/{slug}
```

Expected:

`200 OK`

Result:

Successful.

---

### Update and Change Slug

The current project slug was replaced with a new valid slug.

Expected:

`200 OK`

Result:

Successful.

The previous resource URL returned:

`404 Not Found`

and the new slug returned:

`200 OK`.

---

### Update Missing Project

An update was attempted using a slug that did not exist.

Expected:

`404 Not Found`

Result:

Successful.

---

### Update With Duplicate Slug

An existing project was updated using a slug already owned by another project.

Expected:

`409 Conflict`

Result:

Successful.

---

### Update With Invalid Input

An invalid update request was submitted.

Expected:

`400 Bad Request`

Result:

Successful.

---

### Delete Existing Project

```text
DELETE /api/projects/{slug}
```

Expected:

`204 No Content`

Result:

Successful.

---

### Delete Missing Project

A delete request was sent for a project that no longer existed.

Expected:

`404 Not Found`

Result:

Successful.

---

### GET Missing Project

The GET-by-slug endpoint was also verified after the error-handling refactor.

Expected:

`404 Not Found`

with a structured error response.

Result:

Successful.

---

## Current Project API

The `Project` domain now supports complete CRUD operations:

```text
POST   /api/projects
GET    /api/projects
GET    /api/projects/{slug}
PUT    /api/projects/{slug}
DELETE /api/projects/{slug}
```

The backend can now:

- create projects
- list projects
- retrieve individual projects
- update projects
- change project slugs
- delete projects
- validate request data
- reject duplicate slugs
- return consistent missing-resource errors

This completes the initial CRUD API for the `Project` domain.

---

> **Architectural principle:** Controllers should translate HTTP concerns, services should own application behavior, repositories should own persistence access, and cross-cutting API errors should be handled consistently rather than duplicated across endpoints.