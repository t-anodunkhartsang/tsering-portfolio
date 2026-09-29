# Frontend Setup and Backend Integration

## Goal

Introduce the frontend application and establish the first working connection between the React frontend and Spring Boot backend.

The frontend uses:

- React
- TypeScript
- Vite
- ESLint
- the browser Fetch API

The frontend runs independently from the backend during local development.

```text
Frontend
http://localhost:5173

Backend
http://localhost:8080

PostgreSQL
localhost:5432
```

---

## Frontend Architecture

The application currently follows this structure:

```text
frontend/
├── public/
├── src/
│   ├── api/
│   │   └── projects.ts
│   ├── components/
│   ├── pages/
│   ├── types/
│   │   └── Project.ts
│   ├── App.tsx
│   ├── index.css
│   └── main.tsx
├── package.json
├── tsconfig.json
└── vite.config.ts
```

The folders have different responsibilities.

```text
api/
→ communication with backend APIs

components/
→ reusable UI components

pages/
→ page-level components

types/
→ shared TypeScript data structures
```

This keeps HTTP communication, UI components, and data contracts separate.

---

## React

React was selected as the frontend UI library.

React uses a component-based architecture, allowing the portfolio interface to be divided into reusable pieces.

The application can eventually be structured similarly to:

```text
App
├── Navbar
├── Hero
├── About
├── Experience
├── Projects
│   └── ProjectCard
└── Contact
```

This is preferable to keeping the entire interface inside one large HTML or JavaScript file.

---

## TypeScript

TypeScript is used instead of plain JavaScript.

TypeScript adds static type checking to JavaScript.

This is particularly useful when communicating with the Spring Boot API because the expected shape of API responses can be represented explicitly.

For example:

```ts
export interface ProjectSummary {
  slug: string;
  title: string;
  shortDescription: string;
  githubUrl: string | null;
  liveUrl: string | null;
  imageUrl: string | null;
  featured: boolean;
}
```

This represents the JSON structure returned by the backend's:

`ProjectSummaryResponse`

Conceptually:

```text
Spring Boot
ProjectSummaryResponse
        ↓
       JSON
        ↓
React / TypeScript
ProjectSummary
```

TypeScript helps detect incorrect assumptions about API data during development.

---

## Vite

Vite is used as the frontend build and development tool.

It provides:

- a local development server
- React integration
- TypeScript processing
- hot module replacement
- production builds

The development server runs at:

```text
http://localhost:5173
```

Changes to React components are reflected in the browser without manually rebuilding the project.

---

## Why Vite Instead of Create React App

Create React App was not selected because modern React development commonly uses newer build tools and frameworks.

Vite provides a lightweight development environment with fast startup and development builds.

For this project, a full-stack framework is not required because the backend already exists as a separate Spring Boot application.

The architecture is intentionally:

```text
React frontend
       ↓
REST API
       ↓
Spring Boot backend
```

rather than introducing another server-side frontend framework.

---

## ESLint

ESLint was selected as the JavaScript and TypeScript linting tool.

ESLint provides a mature ecosystem and broad support for React and TypeScript.

OxLint was considered as an alternative.

OxLint offers very fast linting performance, but linting performance is not currently a bottleneck for this relatively small application.

ESLint was preferred because:

- it has a mature plugin ecosystem
- React and TypeScript support is well established
- configuration and troubleshooting resources are widely available
- it is commonly used across frontend projects

---

## Initial Application Cleanup

The default Vite demonstration interface was removed.

The application was reduced to a minimal shell:

```tsx
function App() {
  return (
    <main>
      <h1>Tsering Anodunkhartsang</h1>
      <p>Full-Stack Developer</p>
    </main>
  );
}

export default App;
```

This provides a clean foundation before introducing the actual portfolio design.

The generated demonstration styling was also removed and replaced with a minimal global CSS reset.

---

## Project API Type

The frontend defines:

```text
src/types/Project.ts
```

The initial project summary interface is:

```ts
export interface ProjectSummary {
  slug: string;
  title: string;
  shortDescription: string;
  githubUrl: string | null;
  liveUrl: string | null;
  imageUrl: string | null;
  featured: boolean;
}
```

This mirrors the public API response rather than the backend database entity.

Fields such as:

```text
id
createdAt
updatedAt
displayOrder
```

are not included because they are not returned by the project summary API.

This maintains the same separation already established on the backend:

```text
Database entity
      ≠
REST API contract
      ≠
Frontend type
```

Each layer models only the information it needs.

---

## API Layer

Backend communication is kept outside React components.

The project API function is defined in:

```text
src/api/projects.ts
```

Example:

```ts
import type { ProjectSummary } from "../types/Project";

export async function getProjects(): Promise<ProjectSummary[]> {
  const response = await fetch("/api/projects");

  if (!response.ok) {
    throw new Error("Failed to fetch projects");
  }

  return response.json();
}
```

This keeps HTTP implementation details separate from UI rendering.

Instead of every component calling `fetch()` independently, API communication can be centralized in the `api` directory.

---

## Vite Development Proxy

The browser frontend runs on:

```text
localhost:5173
```

while Spring Boot runs on:

```text
localhost:8080
```

During development, Vite proxies API requests to Spring Boot.

The configuration is defined in:

```text
vite.config.ts
```

with:

```ts
server: {
  proxy: {
    "/api": {
      target: "http://localhost:8080",
      changeOrigin: true,
    },
  },
},
```

The frontend can therefore request:

```ts
fetch("/api/projects")
```

rather than hardcoding:

```ts
fetch("http://localhost:8080/api/projects")
```

The request flow is:

```text
Browser
   ↓
http://localhost:5173/api/projects
   ↓
Vite development proxy
   ↓
http://localhost:8080/api/projects
   ↓
Spring Boot
```

---

## Why Use a Development Proxy?

Without a proxy, the browser would communicate directly between two origins:

```text
localhost:5173
        ↓
localhost:8080
```

This would require Cross-Origin Resource Sharing configuration in Spring Boot during local development.

The Vite proxy avoids unnecessary development-time CORS configuration.

It also prevents frontend source code from depending directly on the backend development port.

The frontend only knows:

```text
/api
```

while Vite determines where those requests should be forwarded during development.

Production deployment may use a different networking configuration.

---

## React State

`App.tsx` currently maintains three pieces of state:

```ts
const [projects, setProjects] = useState<ProjectSummary[]>([]);
const [loading, setLoading] = useState(true);
const [error, setError] = useState<string | null>(null);
```

These represent three possible states of the API request:

```text
loading
   ↓
success
   ↓
projects displayed
```

or:

```text
loading
   ↓
failure
   ↓
error displayed
```

The UI also handles the valid case where the backend returns an empty array.

---

## Fetching Projects

The initial API request is triggered using:

```ts
useEffect(() => {
  getProjects()
    .then(setProjects)
    .catch(() => setError("Could not load projects."))
    .finally(() => setLoading(false));
}, []);
```

The empty dependency array means the effect runs when the component is initially mounted.

The returned projects are stored in React state.

---

## Rendering Projects

Projects are rendered using:

```tsx
projects.map((project) => (
  <article key={project.slug}>
    <h3>{project.title}</h3>
    <p>{project.shortDescription}</p>
  </article>
))
```

The project slug is used as the React key because it uniquely identifies projects in the public API.

This is currently only a temporary representation.

Reusable project components will be introduced as the actual portfolio interface is developed.

---

## Full Request Flow

The first frontend/backend integration was successfully verified.

The complete path is:

```text
React component
      ↓
getProjects()
      ↓
fetch("/api/projects")
      ↓
Vite proxy
      ↓
GET localhost:8080/api/projects
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
JSON response
      ↓
ProjectSummary[]
      ↓
React state
      ↓
Browser rendering
```

This represents the project's first complete full-stack vertical slice.

---

## Verified State

At the end of this milestone:

```text
React application              ✅
TypeScript                     ✅
Vite development server        ✅
ESLint                         ✅
Frontend project structure     ✅
Project API TypeScript type    ✅
API service layer              ✅
Vite backend proxy             ✅
Spring Boot connection         ✅
PostgreSQL data rendered       ✅
```

Project data stored in PostgreSQL can now travel through the entire stack and appear in the browser.

---

## Alternatives Considered

| Decision | Chosen | Alternative | Reason the alternative was not selected |
|---|---|---|---|
| UI library | React | Vue | Vue is a strong frontend framework, but React aligns better with the intended technology profile of this portfolio and existing experience. |
| UI library | React | Angular | Angular provides a comprehensive framework but introduces substantially more framework structure than this portfolio currently requires. |
| Language | TypeScript | JavaScript | JavaScript would work, but TypeScript provides stronger API contracts and compile-time checking as the application grows. |
| Build tool | Vite | Create React App | Vite provides a more modern and lightweight development workflow. |
| Linter | ESLint | OxLint | OxLint is faster, but linting performance is not currently a bottleneck. ESLint provides a larger and more established plugin ecosystem. |
| API communication | Native Fetch API | Axios | The current API requirements are simple enough that the browser's built-in Fetch API is sufficient. Introducing another dependency is unnecessary at this stage. |
| Local API networking | Vite proxy | Direct cross-origin requests | A proxy avoids unnecessary local CORS configuration and keeps backend host information out of frontend source code. |
| Frontend architecture | Separate React app | Server-rendered Spring templates | A separate frontend demonstrates React and TypeScript skills and keeps frontend and backend responsibilities distinct. |

---

> **Architectural principle:** Frontend components should focus on presentation and interaction, while API communication and data contracts remain explicit and separate. Development infrastructure should reduce environment-specific coupling without hiding the boundaries between frontend and backend systems.