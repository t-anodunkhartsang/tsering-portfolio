export interface ProjectSummary {
  slug: string;
  title: string;
  shortDescription: string;
  githubUrl: string | null;
  liveUrl: string | null;
  imageUrl: string | null;
  featured: boolean;
}