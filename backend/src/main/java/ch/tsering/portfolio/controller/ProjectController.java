package ch.tsering.portfolio.controller;

import ch.tsering.portfolio.dto.CreateProjectRequest;
import ch.tsering.portfolio.dto.ProjectDetailResponse;
import ch.tsering.portfolio.dto.ProjectSummaryResponse;
import ch.tsering.portfolio.dto.UpdateProjectRequest;
import ch.tsering.portfolio.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectSummaryResponse> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{slug}")
    public ProjectDetailResponse getProjectBySlug(
            @PathVariable String slug
    ) {
        return projectService.getProjectBySlug(slug);
    }

    @PostMapping
    public ResponseEntity<ProjectDetailResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request
    ) {
        ProjectDetailResponse createdProject =
                projectService.createProject(request);

        URI location = URI.create(
                "/api/projects/" + createdProject.slug()
        );

        return ResponseEntity
                .created(location)
                .body(createdProject);
    }

    @PutMapping("/{slug}")
    public ResponseEntity<ProjectDetailResponse> updateProject(
            @PathVariable String slug,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        ProjectDetailResponse updatedProject =
                projectService.updateProject(slug, request);

        URI location = URI.create(
                "/api/projects/" + updatedProject.slug()
        );

        return ResponseEntity
                .ok()
                .location(location)
                .body(updatedProject);
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable String slug
    ) {
        projectService.deleteProject(slug);

        return ResponseEntity.noContent().build();
    }
}
