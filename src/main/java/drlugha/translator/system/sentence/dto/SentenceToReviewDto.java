package drlugha.translator.system.sentence.dto;

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
