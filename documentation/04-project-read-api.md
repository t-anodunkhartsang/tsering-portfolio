# Project Read API

## Goal

Expose portfolio project data through REST endpoints without returning JPA
entities directly.

---

## Endpoints

### GET `/api/projects`

Returns project summaries.

Response type:

`ProjectSummaryResponse`

The response intentionally omits internal persistence details such as:

- database ID
- display order
- timestamps

### GET `/api/projects/{slug}`

Returns the detailed representation of one project.

Response type:

`ProjectDetailResponse`

If the slug exists:

`200 OK`

If the slug does not exist:

`404 Not Found`

---

## DTO Separation

The REST API does not expose the `Project` JPA entity directly.

Instead:

```text
Project entity
      ↓
Service mapping
      ↓
Response DTO
      ↓
JSON
```
This keeps the persistence model separate from the public API contract.

---

## Summary vs Detail DTO

`ProjectSummaryResponse` is used for project-list views and contains only the
information needed for project cards.

`ProjectDetailResponse` additionally exposes the full project description for a
case-study page.

---

## Service Layer

`ProjectService` retrieves entities through `ProjectRepository` and maps them
into response DTOs.

The repository remains responsible for persistence access, while the service
owns application-level data transformation and ordering.

---

## Repository Queries

ProjectRepository extends:
```Java
JpaRepository<Project, Long>
```
This provides standard CRUD operations.

The custom method:
```java
Optional<Project> findBySlug(String slug);
```
uses Spring Data JPA query derivation.

Spring derives a query based on the entity property name `slug`.

---

## HTTP Status Handling

The detail endpoint uses:
```java
ResponseEntity<ProjectDetailResponse>
```
Existing resource:

`200 OK`

Missing resource:

`404 Not Found`

---

## Verified Request Flow
```
HTTP request
    ↓
Spring MVC
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
DTO mapping
    ↓
Jackson serialization
    ↓
JSON response
```

---

## Verification
The list endpoint initially returned:
```json
[]
```
which confirmed the full read path worked against an empty table.

After inserting one development record into PostgreSQL, the same endpoint
returned the project data.

The detail endpoint was also verified for both an existing slug and a missing
slug.

---

> **Architectural principle:** The public REST API should expose purpose-built
DTOs rather than persistence entities, so internal database changes do not
automatically become API changes.