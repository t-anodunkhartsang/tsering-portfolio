# Database Setup
## Goal

The backend requires a relational database for storing portfolio data such as
projects, technologies, and their relationships.

The local development environment should also be reproducible and should not
depend heavily on machine-specific database configuration.

The chosen setup is:

- PostgreSQL 17
- PostgreSQL running in a Podman container
- Compose for container configuration
- Spring Boot running locally in IntelliJ
- JDBC for communication between Java and PostgreSQL
- Spring Data JPA / Hibernate for persistence
- Flyway for database schema migrations

---

## Initial Spring Boot Failure

The Spring Boot project was initially generated with the following database-related
dependencies:

- Spring Data JPA
- PostgreSQL Driver
- Flyway

Because these dependencies were present, Spring Boot automatically attempted to
configure a datasource during startup.

At this point, no database connection information had been configured yet.

The application therefore failed with an error similar to:

```text
Failed to configure a DataSource:
'url' attribute is not specified and no embedded datasource could be configured.
```
This failure was expected.

It demonstrated that Spring Boot had detected the persistence dependencies and
was attempting to create the database infrastructure required by the application.

---

## Database Architecture

During local development, the backend runs directly on the host machine while
PostgreSQL runs inside a container.

```
macOS
│
├── IntelliJ IDEA
│   └── Spring Boot
│       │
│       │ JDBC
│       │ localhost:5432
│       ▼
│
└── Podman
    └── portfolio-postgres
        └── PostgreSQL 17
            └── portfolio database
```
This provides convenient debugging for the Java application while keeping the
database environment isolated and reproducible.

---

## Why PostgreSQL

PostgreSQL was selected as the relational database for the portfolio.

The application's data has clear relationships.

For example:
```
Project
   │
   └── Technologies
```
A relational database therefore maps naturally to the application's domain.

PostgreSQL also provides a useful future path for the planned AI functionality.
The pgvector extension can later be added to PostgreSQL to store and query
vector embeddings.

This means the project may eventually use the same database for both:
```
PostgreSQL
│
├── relational application data
│
└── vector embeddings
```
without requiring a separate vector database during the early stages of the
project.

---

## Why PostgreSQL Runs in a Container

PostgreSQL was initially installed locally through Homebrew while exploring the
database setup.

However, the project ultimately uses a containerized PostgreSQL instance.

Running the database in a container makes the development environment more
reproducible.

Instead of requiring another developer manually:
```
install PostgreSQL
create a database
create a user
configure permissions
ensure the correct PostgreSQL version is installed
```
the repository defines the database environment using Compose.

The intended workflow becomes:
```
clone repository
      ↓
create local .env
      ↓
start Compose environment
      ↓
PostgreSQL is initialized
```
The Spring Boot backend remains outside the container during development because
running it directly in IntelliJ provides convenient debugging, breakpoints,
automatic recompilation, and log inspection.

---

## Alternatives Considered

| Decision                   | Chosen             | Alternative                                | Reason the alternative was not selected                                                                                                                                         |
| -------------------------- | ------------------ | ------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Database                   | PostgreSQL         | MySQL                                      | MySQL could satisfy the relational requirements, but PostgreSQL also provides a straightforward future path to vector search through `pgvector`.                                |
| Database                   | PostgreSQL         | MongoDB                                    | The portfolio data contains natural relationships between entities, making a relational database a better fit than a document-oriented database.                                |
| Database                   | PostgreSQL         | SQLite                                     | SQLite is useful for lightweight applications but does not represent the deployed backend architecture intended for this project as well as PostgreSQL.                         |
| Local database environment | Container          | Homebrew PostgreSQL                        | A Homebrew installation is machine-specific and requires more manual initialization. A container gives the project a defined PostgreSQL version and reproducible configuration. |
| Container runtime          | Podman             | Docker Desktop                             | Podman was already available in the development environment and provides the container functionality required by the project. Introducing another runtime was unnecessary.      |
| Schema management          | Flyway             | Hibernate `ddl-auto=update`                | Allowing Hibernate to automatically modify production-style schemas makes database changes less explicit. Flyway provides version-controlled and reproducible migrations.       |
| Schema management          | Flyway             | Liquibase                                  | Liquibase is powerful, but Flyway's versioned SQL migration model is simpler and sufficient for the expected schema complexity.                                                 |
| Secret configuration       | `.env` outside Git | Credentials directly in `application.yaml` | Committing credentials would expose secrets in the public repository and Git history.                                                                                           |
| Data persistence           | Named volume       | Container filesystem only                  | Containers are disposable. A named volume allows database data to survive container recreation.                                                                                 |

---

## Container Runtime
Podman is used as the local container runtime.

The installed version was verified using:
```
podman --version
```
Compose support was verified using:
```
podman compose version
```
In the current environment, podman compose delegates Compose processing to an
installed Docker Compose provider.

This does not mean Docker is running the PostgreSQL container. Podman remains the
container runtime while the Compose implementation interprets the Compose file.

---

## Compose Configuration

The database infrastructure is defined in the repository root:
```
tsering-portfolio/
├── backend/
├── documentation/
├── compose.yml
├── .env
├── .env.example
└── .gitignore
```
The Compose configuration is:
```
services:
  postgres:
    image: postgres:17
    container_name: portfolio-postgres

    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}

    ports:
      - "5432:5432"

    volumes:
      - portfolio_postgres_data:/var/lib/postgresql/data

    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 5s
      timeout: 5s
      retries: 5

    restart: unless-stopped

volumes:
  portfolio_postgres_data:
```

---

## PostgreSQL Image
The database service uses:
```YAML
image: postgres:17
```
This explicitly defines the PostgreSQL major version used by the project.

A developer starting the project therefore receives a PostgreSQL 17 environment
instead of relying on whichever PostgreSQL version happens to be installed on
their machine.

---

## Environment Variables and Secrets
Database configuration is stored locally in:
```
.env
```
with values such as:
```
POSTGRES_DB=portfolio
POSTGRES_USER=portfolio_app
POSTGRES_PASSWORD=<local-password>
```
The .env file is excluded from Git:
```
.env
```
The real database password therefore does not enter the public repository.

A safe template is committed instead:
```
.env.example
```
Example:
```
POSTGRES_DB=portfolio
POSTGRES_USER=portfolio_app
POSTGRES_PASSWORD=change-me
```
A developer cloning the repository can create their local configuration using:
```Bash
cp .env.example .env
```
and replace the placeholder password.

---

## Database Initialisation
The official PostgreSQL image uses the following environment variables during
initial database initialization:
```
POSTGRES_DB
POSTGRES_USER
POSTGRES_PASSWORD
```
For this project they create:
```
Database:
portfolio

Application database user:
portfolio_app
```
The database therefore does not need to be manually created with SQL during the
initial container setup.

The initialization happens when PostgreSQL starts with a new data volume.

---

## Port Mapping

The Compose configuration contains:
```YAML
ports:
- "5432:5432"
```
This maps:
```
Host machine                PostgreSQL container

localhost:5432  ──────────→ 5432
```
Because Spring Boot currently runs directly on macOS, it connects to:
```
localhost:5432
```
If the backend is containerized later, the networking model will change.

Containers in the same Compose network can communicate through their service
names. The backend would then typically connect to something such as:
```
postgres:5432
```
instead of `localhost:5432`.

---

## Persistent Database Storage

PostgreSQL data is stored using a named volume:
```YAML
volumes:
- portfolio_postgres_data:/var/lib/postgresql/data
```
The container itself should be treated as disposable.

Without persistent storage:
```
container removed
↓
container filesystem removed
↓
database data may be lost
```
With the named volume:
```
PostgreSQL container
│
▼
portfolio_postgres_data
│
▼
persistent database files
```
The container can therefore be replaced while retaining its database state.

---

## Database Health Check

The PostgreSQL service contains:
```YAML
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
  interval: 5s
  timeout: 5s
  retries: 5
```
A running container does not necessarily mean the application inside it is ready.

The health check verifies that PostgreSQL itself is ready to accept database
connections.

The running container was verified with:
```Bash
podman ps
```
which reported:
```
portfolio-postgres
Up ... (healthy)
```

---

## Connecting Directly to PostgreSQL

The database was verified from inside the container using:
```Bash
podman exec -it portfolio-postgres \
  psql -U portfolio_app -d portfolio
```
The current database user and database were verified with:
```SQL
SELECT current_user, current_database();
```
Result:
```
current_user  | current_database
--------------+-----------------
portfolio_app | portfolio
```
This confirmed that the expected application identity and database were created
successfully.

---

## Spring Boot Datasource Configuration

Spring Boot connects to PostgreSQL through configuration in:
```
backend/src/main/resources/application.yaml
```
The relevant configuration is:
```YAML
spring:
  application:
    name: portfolio

  config:
    import: "optional:file:../.env[.properties]"

  datasource:
    url: jdbc:postgresql://localhost:5432/${POSTGRES_DB}
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false

  flyway:
    enabled: true
```

---

## Loading the .env File

The backend imports the repository-level .env file with:
```YAML
spring:
    config:
      import: "optional:file:../.env[.properties]"
```
The Spring Boot application's working directory is:
```
tsering-portfolio/backend/
```
Therefore:
```
../.env
```
resolves to:
```
tsering-portfolio/.env
```
The `[.properties]` hint tells Spring to interpret the extensionless `.env` file
using Java properties-style `KEY=value` syntax.

The `optional:` prefix allows the application configuration to support other
environments where the variables may be provided directly by the operating
system or deployment platform instead of through a local `.env` file.

---

## Development Run Configuration

The IntelliJ Spring Boot run configuration uses:
```
JDK:
Java 21

Module/classpath:
portfolio

Main class:
ch.tsering.portfolio.PortfolioApplication

Working directory:
tsering-portfolio/backend
```
The working directory is important because the .env import uses a relative path.

During setup, the application was temporarily launched from:
```
tsering-portfolio/
```
instead of:
```
tsering-portfolio/backend/
```
As a result, Spring searched for ../.env in the wrong location.

The configuration values were therefore not resolved.

PostgreSQL received the literal value:
```
${POSTGRES_USER}
```
as the database username and rejected the authentication attempt.

Changing the working directory to `backend/` allowed Spring to locate the correct
`.env` file.

This provided a useful demonstration that relative configuration paths depend on
the application's working directory.

---

## JDBC Connection

The datasource URL is:
```YAML
url: jdbc:postgresql://localhost:5432/${POSTGRES_DB}
```
During local development this resolves to:
```
jdbc:postgresql://localhost:5432/portfolio
```
The components are:
```
jdbc:postgresql://localhost:5432/portfolio
│    │            │         │    │
│    │            │         │    └─ database name
│    │            │         └────── port
│    │            └──────────────── hostname
│    └───────────────────────────── database driver/protocol
└────────────────────────────────── Java Database Connectivity
```
JDBC stands for Java Database Connectivity.

The complete persistence path is:
```
Spring Data JPA
↓
Hibernate
↓
JDBC
↓
PostgreSQL JDBC Driver
↓
localhost:5432
↓
Podman port mapping
↓
PostgreSQL
```

---

## HikariCP

Spring Boot uses HikariCP as its database connection pool.

Instead of establishing a new database connection for every operation, HikariCP
maintains a pool of reusable connections.

Conceptually:
```
Spring application
↓
HikariCP connection pool
↓
PostgreSQL connections
↓
PostgreSQL
```
A successful Hikari startup confirms that Spring Boot can establish its database
connections.

---

## JPA and Hibernate

Spring Data JPA provides the persistence abstraction used by the application.

Hibernate acts as the JPA implementation underneath it.

The intended architecture is:
```
Controller
↓
Service
↓
Repository
↓
Spring Data JPA
↓
Hibernate
↓
JDBC
↓
PostgreSQL
```
At this stage there are no repository interfaces yet, which is expected.

---

## Why ddl-auto Is Set to validate

The JPA configuration contains:
```YAML
jpa:
hibernate:
ddl-auto: validate
```
Hibernate therefore verifies that the database schema matches the application's
entity mappings but does not automatically modify the schema.

Database schema ownership belongs to Flyway.

The intended separation is:
```
Flyway
↓
creates and modifies schema

Hibernate
↓
validates entity mappings against schema
```
This makes database changes explicit and version-controlled.

Using:
```
ddl-auto: update
```
would allow Hibernate to automatically mutate the schema.

That is convenient for very early prototypes but provides less control over
database evolution.

---

## Why Open Session in View Is Disabled

The configuration contains:
```YAML
open-in-view: false
```
The intention is to keep persistence operations within the appropriate service
and persistence layers instead of allowing database access to occur implicitly
while web responses are being serialized.

The preferred direction is:
```
Controller
↓
Service
↓
Repository
↓
Database
```
rather than allowing persistence concerns to leak into the HTTP response layer.

---

## Flyway

Flyway manages database schema migrations.

Future migrations will be stored under:
```
backend/src/main/resources/db/migration/
```
and will use filenames such as:
```
V1__create_projects_table.sql
V2__create_technologies_table.sql
V3__create_project_technology_relation.sql
```
Flyway records which migrations have already been executed.

This allows a new environment to reproduce the application's schema by applying
the same migration history.

---

## Flyway Verification

After Spring Boot successfully connected to PostgreSQL, the database tables were
listed using:
```SQL
\dt
```
The result contained:
```
public | flyway_schema_history | table | portfolio_app
```
No application tables exist yet.

The presence of:
```
flyway_schema_history
```
confirms that Flyway successfully connected to the database and initialized its
schema history.

This table is maintained by Flyway and records migration information such as
migration versions, descriptions, execution order, and success state.

Application tables will be created through future Flyway migration files.

---

## Current Verified State

At the end of this setup milestone:
```
Java 21                    ✅
Spring Boot                ✅
Maven dependencies         ✅
Podman                     ✅
PostgreSQL 17 container    ✅
Container health check     ✅
Persistent volume          ✅
Environment configuration ✅
JDBC connection            ✅
HikariCP                   ✅
Flyway                     ✅
```
The database currently contains only Flyway's schema-history table.

The next database step will be creating the first versioned migration for the
portfolio domain model.

> **Architectural principle:** Infrastructure should be reproducible and explicit.
External dependencies such as databases should be configured as part of the
project environment, while secrets remain outside source control. Schema changes
should be version-controlled rather than applied manually.