package ch.tsering.portfolio.exception;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String slug) {
        super("Project with Slug '" + slug + "' was not found.");
    }
}
