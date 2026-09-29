import { useEffect, useState } from "react";
import { getProjects } from "./api/projects";
import type { ProjectSummary } from "./types/Project";

function App() {
  const [projects, setProjects] = useState<ProjectSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getProjects()
      .then(setProjects)
      .catch(() => setError("Could not load projects."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <main>
      <h1>Tsering Anodunkhartsang</h1>
      <p>Full-Stack Developer</p>

      <h2>Projects</h2>

      {loading && <p>Loading projects...</p>}

      {error && <p>{error}</p>}

      {!loading && !error && projects.length === 0 && (
        <p>No projects found.</p>
      )}

      {!loading &&
        !error &&
        projects.map((project) => (
          <article key={project.slug}>
            <h3>{project.title}</h3>
            <p>{project.shortDescription}</p>
          </article>
        ))}
    </main>
  );
}

export default App;