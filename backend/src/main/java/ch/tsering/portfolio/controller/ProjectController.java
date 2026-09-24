package ch.tsering.portfolio.controller;

import ch.tsering.portfolio.dto.ProjectDetailResponse;
import ch.tsering.portfolio.dto.ProjectSummaryResponse;
import ch.tsering.portfolio.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ProjectDetailResponse> getProjectBySlug(
            @PathVariable String slug
    ) {
        return projectService.getProjectBySlug(slug)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }
}
