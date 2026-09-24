package ch.tsering.portfolio.service;

import ch.tsering.portfolio.dto.ProjectDetailResponse;
import ch.tsering.portfolio.dto.ProjectSummaryResponse;
import ch.tsering.portfolio.entity.Project;
import ch.tsering.portfolio.repository.ProjectRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository, ProjectRepository projectRepository1) {
        this.projectRepository = projectRepository1;
    }

    @Transactional(readOnly = true)
    public List<ProjectSummaryResponse> getAllProjects() {
        Sort sort = Sort.by(
                Sort.Order.asc("displayOrder"),
                Sort.Order.asc("title")
        );

        return projectRepository.findAll(sort)
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProjectDetailResponse> getProjectBySlug(String slug) {
        return projectRepository.findBySlug(slug)
                .map(this::toDetailResponse);
    }

    private ProjectSummaryResponse toSummaryResponse(Project project) {
        return new ProjectSummaryResponse(
                project.getSlug(),
                project.getTitle(),
                project.getShortDescription(),
                project.getGithubUrl(),
                project.getLiveUrl(),
                project.getImageUrl(),
                project.isFeatured()
        );
    }

    private ProjectDetailResponse toDetailResponse(Project project) {
        return new ProjectDetailResponse(
                project.getSlug(),
                project.getTitle(),
                project.getShortDescription(),
                project.getDescription(),
                project.getGithubUrl(),
                project.getLiveUrl(),
                project.getImageUrl(),
                project.isFeatured()
        );
    }
}
