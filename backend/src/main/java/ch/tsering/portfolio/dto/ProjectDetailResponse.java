package ch.tsering.portfolio.dto;

public record ProjectDetailResponse(
        String slug,
        String title,
        String shortDescription,
        String description,
        String githubUrl,
        String liveUrl,
        String imageUrl,
        boolean featured
) {
}