package ch.tsering.portfolio.exception;

public class DuplicateProjectSlugException extends RuntimeException {

    public DuplicateProjectSlugException(String slug) {
        super("A project with slug '" + slug + "' already exists.");
    }
}
