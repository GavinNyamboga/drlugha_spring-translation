package kmusau.translator.DTOs.translatedSentencesDTOs;

import lombok.Data;

import java.util.List;

@Data
public class SentenceToReviewDto {
    private Long batchDetailsId;
    private String language;

    private String batchType;
    private List<TranslatedSentenceItemDto> unreviewedSentences;
    private List<TranslatedSentenceItemDto> reviewedSentences;
}
