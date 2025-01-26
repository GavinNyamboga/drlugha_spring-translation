package drlugha.translator.system.stats.dto;

public interface BatchDetailsStatsDto {
    Long getModeratorApprovedSentences();

    Long getModeratorRejectedSentences();

    Long getExpertApprovedSentences();

    Long getExpertRejectedSentences();
}
