# Backend Project Setup Decisions

### Project type: Maven 
The backend uses **Maven** as its build and dependency-management tool.

Maven is a strong choice for a Spring Boot because the Spring ecosystem has excellent Maven support, and the project structure is predictable. Dependencies are declared in one `pom.xml` file, which makes it easy to understand what libraries the application uses. 

For this portfolio, Maven will manage dependencies such as Spring Web, Spring Data JPA, PostgreSQL, Flyway, Validation, and eventually Spring AI.

It also gives us the Maven Wrapper:
```
mvnm.cmd
```
This means someone cloning the project does not necessarily need Maven installed globally. They can run:
```
./mvnw spring-boot:run
```
That is particularly useful for reproducibility and CI/CD later.

We could have used Gradle, but Maven is simpler for this project's needs and is extremely common in Java/Spring projects.

---

### Language: Java
The backend is written in Java because the main purpose of this portfolio is partly to demonstrate backend development skills with Java and Spring Boot.

Java gives us:
```
Strong static typing
Object-oriented programming
Large ecosystem
Mature testing tools
Strong Spring integration
Widely used enterprise/backend technology
```
Using Java also aligns the portfolio with the type of backend/full-stack roles we want the project to demonstrate.

Kotlin is also supported by Spring, but using it here would add another language without providing a meaningful advantage for this project.

Groovy is not necessary either.

---

### Java target: Java 21
Even though the development machine currently has:
```
Java 24
```
installed, the application targets:
```
Java 21
```
Java 21 is an **LTS — Long-Term Support — release**.

That makes it a good target for a backend application because it is intended to remain supported for much longer than short-lived feature releases.

It also avoids requiring everyone who runs the project to have the newest Java release installed.

Conceptually:
```
My machine
Java 24
    │
    │ can compile/run Java 21-targeted code
    ▼
Portfolio application
Java 21
```
So the installed JDK and the Java version targeted by the project do not need to be identical.

This gives us modern Java functionality while keeping the project on a stable and commonly used release.

---

### Spring Boot: 4.1.1
The backend uses **Spring Boot 4.1.1**.

Spring Boot sits on top of the Spring Framework and removes much of the repetitive setup normally required to create a Java web application.

Without Spring Boot, we would manually configure significantly more infrastructure such as:
```
Web server configuration
Dependency wiring
Application startup
Database integration
HTTP infrastructure
Serialization
Configuration loading
```
Spring Boot provides sensible defaults while still allowing us to customize those components when necessary.

For example, our application can start from:
```Java
@SpringBootApplication
public class PortfolioApplication {

    static void main(String[] args) {
        SpringApplication.run(PortfolioApplication.class, args);
    }
}
```
Spring Boot will eventually handle the embedded web server, REST controllers, dependency injection, JPA integration, configuration, and other infrastructure around our application.

#### Why 4.1.1 instead of the other initializr options?
We deliberately choose a **stable release**.

We avoided version marked:
```
SNAPSHOT
M1
```
because they represent development or milestone releases.

For example:
```
4.2.0 SNAPSHOT
```
contains development work for a future release and may change.

For a portfolio project, stability is more important than experimenting with unreleased framework features.

---

### Group: `ch.tsering`
The Maven group is:
```
ch.tsering
```
The group acts as the namespace of the project.

Java projects traditionally use hierarchical identifiers such as:
```
com.company
org.organization
ch.name
```
Using:
```
ch.tsering
```
gives the project a namespace associated with me rather than using something generic such as:
```
com.example
```

---

### Artifact: `portfolio`
The Maven artifact is:
```
portfolio
```
The artifact identifies the actual application inside the Maven project.

For example, once Maven builds the project, we may eventually get something similar to:
```
portfolio-0.0.1-SNAPSHOT.jar
```

So:
```
Group:
ch.tsering

Artifact:
portfolio
```
together identify the project.

Conceptually:
```
ch.tsering:portfolio
```

---

### Package name: `ch.tsering.portfolio`
The root Java package is:
```
ch.tsering.portfolio
```

This results in a source structure such as:
```
src/main/java/
└── ch/
    └── tsering/
        └── portfolio/
            └── PortfolioApplication.java
```

Later, we can organize the application further:
```
ch.tsering.portfolio

├── controller
├── service
├── repository
├── entity
├── dto
├── config
└── exception
```

---

### Packaging: JAR
The application uses:
```
Jar
```

rather than:
```
War
```
Spring Boot applications usually run as self-contained executable JARs.

After building the application, we can eventually run something similar to:
```Bash
java -jar portfolio.jar
```
Spring Boot includes an embedded web server, so we do not need to deploy the application manually into an external Tomcat server.

A WAR is more relevant when an application must be deployed into an existing external application server.

That is not necessary for this project.

Our architecture will instead be:
```
Spring Boot application
        │
        └── Embedded web server
                │
                └── REST API
```
This is also much easier to containerize later with Docker.

---

### Configuration format: YAML
We selected:
```
YAML
```
instead of:
```
Properties
```
Both formats work with Spring Boot.

The main reason for choosing YAML is readability once the application's configuration becomes more complex.

For example:
```YAML
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/portfolio
    username: portfolio

  jpa:
    hibernate:
      ddl-auto: validate
```

The equivalent properties file would look more flattened:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/portfolio
spring.datasource.username=portfolio
spring.jpa.hibernate.ddl-auto=validate
```
Neither is technically better, but YAML becomes easier to navigate when the application later contains configuration for:
```
PostgreSQL
JPA
Flyway
CORS
AI
Security
Logging
Deployment environments
```
So our main configuration file will be:
```
src/main/resources/application.yml
```

---

## Dependencies
### Spring Web
Spring Web is included because the backend will expose a REST API.

For example:
```http
GET /api/projects
```
will eventually be handled by code similar to:
```Java
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @GetMapping
    public List<ProjectDto> getProjects() {
    }
}
```
The React frontend will communicate with these REST endpoints over HTTP.

Our architecture will therefore begin as:
```
React
   │
   │ HTTP / JSON
   ▼
Spring Boot REST API
```

---

### Spring Data JPA
Spring Data JPA will manage communication between our Java domain model and the relational database.

Instead of manually writing SQL every time we need to retrieve a project, we can define entities and repositories.

For example:
```Java
@Entity
public class Project {

    @Id
    @GeneratedValue
    private Long id;

    private String title;
}
```
and later:
```Java
public interface ProjectRepository
        extends JpaRepository<Project, Long> {
}
```
The rough architecture becomes:
```
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
JPA is the Java persistence specification, while Hibernate is the ORM implementation commonly used underneath Spring Data JPA.

We will explore that distinction more when we actually create our first entity.

---

### PostgreSQL Driver
The application will use PostgreSQL as its relational database.

The PostgreSQL JDBC driver allows the Java application to communicate with PostgreSQL.

Eventually:
```
Spring Boot
     │
     │ JDBC
     ▼
PostgreSQL
```
PostgreSQL is a particularly useful choice for this portfolio because our initial data is strongly relational.

For example:
```
Project
    │
    └── Technologies
```
and:
```
Project
    │
    ├── title
    ├── description
    ├── GitHub URL
    ├── technologies
    └── display order
```
There is also a second reason PostgreSQL fits our longer-term architecture.

When we introduce the AI/RAG part of the portfolio, PostgreSQL can later be extended with pgvector for storing and searching embeddings.

That means we should not need an entirely separate vector database just for the AI feature.

Eventually:
```
PostgreSQL
│
├── normal relational data
│   ├── projects
│   └── technologies
│
└── vector data
    └── portfolio document embeddings
```
So the database decision works for both the conventional backend and our later AI architecture.

---

### Flyway Migration
Flyway is used for database migrations.

A common mistake in small projects is to manually modify the database whenever the schema changes.

For example:
```
Day 1:
Create project table manually

Day 5:
Add github_url manually

Day 12:
Add technology table manually
```
The problem is that there is no reliable record of how the database reached its current structure.

Flyway solves this by putting database changes into versioned files.

For example:
```
db/migration/

V1__create_project_table.sql
V2__create_technology_table.sql
V3__create_project_technology_table.sql
```
When the application starts, Flyway knows which migrations have already been applied and which new migrations need to run.

This means the database structure becomes part of our source code and Git history.

That is important because someone should eventually be able to:
```
Clone repository
      ↓
Start database
      ↓
Start Spring Boot
      ↓
Flyway creates required schema
      ↓
Application works
```
without manually recreating the database structure.

---

### Validation
Spring Validation will validate data entering our application.

For example, imagine our admin functionality eventually sends:
```JSON
{
  "title": "",
  "description": ""
}
```
We should not allow an empty project title.

We can create a request DTO such as:
```Java
public class CreateProjectRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;
}
```
Spring can then reject invalid input before it reaches the business logic.

This creates a clean separation:
```
HTTP request
     ↓
Validation
     ↓
Controller
     ↓
Service
     ↓
Database
```
rather than letting bad data travel throughout the application.

---

### Why we did not  add everything immediately
Another intentional architectural decision is keeping the initial dependency set small.

We know the application will eventually probably use:
```
Spring Security
Spring AI
pgvector
Docker
AI model integration
Possibly authentication
```
but we are not adding those yet.

The reason is not simply convenience.

We're building the system incrementally.

First:
```
Spring Boot
     ↓
REST
     ↓
PostgreSQL
```
Then:
```
React
     ↓
Spring Boot
     ↓
PostgreSQL
```
Then:
```
React
     ↓
Spring Boot
     ├── PostgreSQL
     │
     └── Spring AI
             ↓
          LLM / RAG
```
This gives each dependency a clear reason to exist.

It also makes debugging much easier because when something breaks, there are fewer moving parts.

For this portfolio in particular, that matters: the objective isn't to show that we can put twenty technologies in a `pom.xml`. The objective is to be able to explain why each technology exists and what problem it solves.

---

## Alternative technologies considered
The technologies below are not necessarily worse than the selected technologies. They were rejected because they were less suitable for this project's goals, scope, and learning objectives.

| Decision                       | Chosen                                     | Alternative                    | Why the alternative was not chosen                                                                                                                                                                                                             |
| ------------------------------ | ------------------------------------------ | ------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Build tool                     | **Maven**                                  | Gradle                         | Gradle is powerful and flexible, but that flexibility is not necessary for this project. Maven's predictable structure and `pom.xml` make dependency management easier to understand and document.                                             |
| Backend language               | **Java**                                   | Kotlin                         | Kotlin integrates very well with Spring and is more concise, but the portfolio is specifically intended to demonstrate Java backend skills. Adding Kotlin would introduce another language without solving a problem we currently have.        |
| Backend language               | **Java**                                   | Groovy                         | Groovy offers dynamic language features and is used in parts of the JVM ecosystem, but it is less relevant to the backend skills this portfolio is intended to demonstrate.                                                                    |
| Java target                    | **Java 21**                                | Java 24                        | Java 24 is installed locally, but using the newest available JDK would unnecessarily increase the runtime requirement. Java 21 is an LTS release and is a more conservative application target.                                                |
| Framework                      | **Spring Boot**                            | Plain Spring Framework         | Plain Spring would require considerably more configuration. Spring Boot gives us sensible defaults while still allowing access to the Spring ecosystem underneath.                                                                             |
| Framework                      | **Spring Boot**                            | Jakarta EE                     | Jakarta EE could also be used for enterprise Java applications, but Spring Boot better matches the technologies this portfolio is intended to demonstrate and provides a very straightforward standalone application model.                    |
| Framework                      | **Spring Boot**                            | Quarkus                        | Quarkus is attractive for cloud-native Java and fast startup, but introducing it would shift the project's focus away from demonstrating Spring knowledge.                                                                                     |
| Framework                      | **Spring Boot**                            | Micronaut                      | Micronaut is lightweight and has strong dependency-injection features, but Spring has a larger ecosystem for the features planned here, particularly Spring Data, Spring Security and Spring AI.                                               |
| Spring version                 | **Stable Spring Boot release**             | SNAPSHOT                       | SNAPSHOT versions are development builds and can change before release. There is no feature in this portfolio that justifies accepting that instability.                                                                                       |
| Spring version                 | **Stable Spring Boot release**             | Milestone / RC                 | Milestone and release-candidate builds are useful for testing upcoming versions, but a portfolio project benefits more from predictable stable dependencies.                                                                                   |
| Packaging                      | **JAR**                                    | WAR                            | WAR packaging is mainly useful when deploying into an external application server. Spring Boot can run as a self-contained executable JAR with an embedded server, which better fits our deployment model.                                     |
| Configuration                  | **YAML**                                   | `.properties`                  | Properties files work equally well, but configuration becomes increasingly flattened as the application grows. YAML makes hierarchical configuration easier to read for PostgreSQL, JPA, AI and deployment settings.                           |
| Web stack                      | **Spring MVC / Spring Web**                | Spring WebFlux                 | WebFlux is designed around reactive, non-blocking applications. The portfolio does not currently have workloads requiring a reactive architecture, so introducing Reactor would add complexity without meaningful benefit.                     |
| API style                      | **REST**                                   | GraphQL                        | GraphQL would be useful for clients that need highly flexible querying, but the portfolio has a small and predictable API. REST is simpler and sufficient for the planned frontend.                                                            |
| API style                      | **REST**                                   | gRPC                           | gRPC is excellent for service-to-service communication, but a browser-based React frontend works naturally with HTTP/JSON REST endpoints.                                                                                                      |
| Database                       | **PostgreSQL**                             | MySQL                          | MySQL could easily support the relational portion of this project. PostgreSQL was preferred partly because of its rich feature set and because we can later use `pgvector` for AI embeddings without introducing a second database technology. |
| Database                       | **PostgreSQL**                             | MongoDB                        | Most portfolio data has clear relationships between projects, technologies and other entities. A relational database maps naturally to those relationships, so a document database provides little advantage here.                             |
| Database                       | **PostgreSQL**                             | SQLite                         | SQLite would make local development simple, but PostgreSQL better represents the kind of database architecture used in deployed backend applications and better supports our planned AI/vector functionality.                                  |
| Database                       | **PostgreSQL**                             | H2                             | H2 is useful for testing and prototypes, but using it as the primary database could hide differences that appear when deploying to PostgreSQL. We prefer developing against the same database technology we intend to deploy.                  |
| Persistence                    | **Spring Data JPA**                        | Raw JDBC                       | JDBC would provide more direct SQL control, but it would require considerable repetitive mapping and persistence code. JPA better suits the relatively standard relational operations needed in this application.                              |
| Persistence                    | **Spring Data JPA**                        | Spring Data JDBC               | Spring Data JDBC is simpler than JPA and avoids some ORM complexity, but JPA provides richer relationship mapping that will be useful for entities such as projects and technologies.                                                          |
| ORM                            | **Hibernate via JPA**                      | jOOQ                           | jOOQ provides excellent type-safe SQL and would be attractive for SQL-heavy applications. This project's queries are expected to be relatively straightforward, making JPA a better fit for the current scope.                                 |
| Database migrations            | **Flyway**                                 | Liquibase                      | Liquibase is powerful and supports XML, YAML, JSON and SQL changelogs. Flyway's versioned SQL migration model is simpler and sufficient for this relatively small schema.                                                                      |
| Database migrations            | **Flyway**                                 | Hibernate `ddl-auto=update`    | Automatic schema mutation is convenient during prototyping but makes database evolution less explicit and reproducible. Versioned Flyway migrations give us a reliable history of schema changes.                                              |
| Input validation               | **Jakarta Validation / Spring Validation** | Manual validation              | Manually checking every field inside controllers or services would introduce repetitive logic. Declarative annotations such as `@NotBlank` keep validation rules clear and close to the DTO definition.                                        |
| Frontend/backend communication | **JSON over HTTP**                         | Server-rendered Thymeleaf      | Thymeleaf could produce the entire portfolio from Spring Boot, but we specifically want to demonstrate React and TypeScript frontend development, so the frontend and backend are intentionally separated.                                     |
| Frontend architecture          | **Separate React application**             | Spring Boot static HTML/CSS/JS | Serving static frontend files from Spring would be simpler, but it would not demonstrate a modern separated frontend architecture or TypeScript/React skills.                                                                                  |
| AI integration later           | **Spring AI**                              | Separate Python AI backend     | Python has an excellent AI ecosystem, but introducing another backend service would increase deployment and architectural complexity. Spring AI lets us integrate LLM features while keeping the primary backend in Java.                      |
| Vector storage later           | **PostgreSQL + pgvector**                  | Dedicated vector database      | Dedicated vector databases can be useful at large scale, but our dataset will be small. Using pgvector avoids operating another database while still demonstrating vector similarity search.                                                   |

#### A few alternatives we deliberately postponed
Some technologies are not being rejected permanently; they simply don't solve a problem we have yet.

| Technology                  | Current decision    | When we would introduce it                                                                         |
| --------------------------- | ------------------- | -------------------------------------------------------------------------------------------------- |
| **Spring Security**         | Not added initially | When we implement an admin dashboard, authentication or protected write endpoints                  |
| **Spring AI**               | Not added initially | After the basic React → Spring → PostgreSQL application works                                      |
| **pgvector**                | Not added initially | When implementing RAG and semantic search                                                          |
| **Docker / Docker Compose** | Postponed           | Once Spring and PostgreSQL work locally and we want reproducible environments                      |
| **Redis**                   | Not needed          | If later we need caching, distributed sessions, rate limiting or temporary state                   |
| **WebSocket**               | Not needed          | If we introduce genuinely real-time functionality                                                  |
| **Kafka / RabbitMQ**        | Not needed          | If the system gains asynchronous workflows that justify message queues                             |
| **Kubernetes**              | Not needed          | The application is far too small to justify container orchestration                                |
| **Microservices**           | Not needed          | The application does not contain independently scalable domains that justify splitting the backend |
| **GraphQL**                 | Not needed          | REST endpoints currently satisfy the frontend's data requirements                                  |

---

> Architectural principle:** Prefer the simplest technology that satisfies the current requirement while leaving a reasonable path for future features. Technologies are added when they solve an identified problem, not simply to increase the number of technologies used in the project.