package ch.tsering.portfolio.service;

import ch.tsering.portfolio.dto.CreateProjectRequest;
import ch.tsering.portfolio.dto.ProjectDetailResponse;
import ch.tsering.portfolio.dto.ProjectSummaryResponse;
import ch.tsering.portfolio.dto.UpdateProjectRequest;
import ch.tsering.portfolio.entity.Project;
import ch.tsering.portfolio.exception.DuplicateProjectSlugException;
import ch.tsering.portfolio.exception.ProjectNotFoundException;
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
    public ProjectDetailResponse getProjectBySlug(String slug) {
        Project project = getProjectEntityBySlug(slug);
        return toDetailResponse(project);
    }

    @Transactional
    public ProjectDetailResponse createProject(CreateProjectRequest request) {

        if (projectRepository.existsBySlug(request.slug())) {
            throw new DuplicateProjectSlugException(request.slug());
        }

        Project project = new Project();

        project.setSlug(request.slug());
        project.setTitle(request.title());
        project.setShortDescription(request.shortDescription());
        project.setDescription(request.description());
        project.setGithubUrl(request.githubUrl());
        project.setLiveUrl(request.liveUrl());
        project.setImageUrl(request.imageUrl());
        project.setFeatured(request.featured());
        project.setDisplayOrder(request.displayOrder());

        Project savedProject = projectRepository.save(project);

        return toDetailResponse(savedProject);
    }

    @Transactional
    public ProjectDetailResponse updateProject(
            String currentSlug,
            UpdateProjectRequest request
    ) {
        Project project = getProjectEntityBySlug(currentSlug);

        if (projectRepository.existsBySlugAndIdNot(
                request.slug(),
                project.getId()
        )) {
            throw new DuplicateProjectSlugException(request.slug());
        }

        project.setSlug(request.slug());
        project.setTitle(request.title());
        project.setShortDescription(request.shortDescription());
        project.setDescription(request.description());
        project.setGithubUrl(request.githubUrl());
        project.setLiveUrl(request.liveUrl());
        project.setImageUrl(request.imageUrl());
        project.setFeatured(request.featured());
        project.setDisplayOrder(request.displayOrder());

        Project updatedProject = projectRepository.save(project);

        return toDetailResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(String slug) {
        Project project = getProjectEntityBySlug(slug);
        projectRepository.delete(project);
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

    private Project getProjectEntityBySlug(String slug) {
        return projectRepository.findBySlug(slug)
                .orElseThrow(() -> new ProjectNotFoundException(slug));
    }
}
