package drlugha.translator.system.sentence.dto;

import lombok.Data;

import java.util.List;

@Data
public class VoicesToReviewDto {
    private Long batchDetailsId;
    private String language;
    private List<TranslatedSentenceItemDto> reviewedAudios;
    private List<TranslatedSentenceItemDto> unreviewedAudios;
}
