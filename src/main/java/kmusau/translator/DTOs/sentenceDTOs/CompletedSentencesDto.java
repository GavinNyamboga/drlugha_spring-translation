package kmusau.translator.DTOs.sentenceDTOs;

import lombok.Data;

import java.util.List;

/**
 * A DTO for holding sentences that have passed through all stages
 */
@Data
public class CompletedSentencesDto {
    private Long batchDetailsId;
    private String batchDetailsStatus;

    private String batchType;
    private String language;
    private Integer numberOfSentences;
    private Integer numberOfCompletedSentences;
    private List<CompletedSentenceItemDto> completedSentences;
}
