package ch.tsering.portfolio.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank
        @Size(max = 150)
        String slug,

        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 500)
        String shortDescription,

        @NotBlank
        String description,

        @Size(max = 500)
        String githubUrl,

        @Size(max = 500)
        String liveUrl,

        @Size(max = 500)
        String imageUrl,

        boolean featured,

        @Min(0)
        int displayOrder
) {
}
