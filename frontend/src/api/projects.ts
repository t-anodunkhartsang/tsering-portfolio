import type { ProjectSummary } from "../types/Project";

export async function getProjects(): Promise<ProjectSummary[]> {
  const response = await fetch("/api/projects");

  if (!response.ok) {
    throw new Error("Failed to fetch projects");
  }

  return response.json();
}