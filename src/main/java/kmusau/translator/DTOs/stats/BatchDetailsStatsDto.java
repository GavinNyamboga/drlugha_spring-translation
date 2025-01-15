package kmusau.translator.DTOs.stats;

public interface BatchDetailsStatsDto {
    Long getModeratorApprovedSentences();
    Long getModeratorRejectedSentences();
    Long getExpertApprovedSentences();
    Long getExpertRejectedSentences();
}
