package ch.tsering.portfolio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateProjectSlugException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateProjectSlug(
            DuplicateProjectSlugException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "duplicate_project_slug",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fields = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fields.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", "validation_failed");
        response.put("fields", fields);

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProjectNotFound(
            ProjectNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "project_not_found",
                        "message", exception.getMessage()
                ));
    }
}
