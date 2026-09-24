package ch.tsering.portfolio.dto;

public record ProjectSummaryResponse(
        String slug,
        String title,
        String shortDescription,
        String githubUrl,
        String liveUrl,
        String imageUrl,
        boolean featured
) {
}
