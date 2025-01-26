package drlugha.translator.system.sentence.dto;

import lombok.Data;

import java.util.List;

@Data
public class SentenceToRecordDto {
    private Long batchDetailsId;
    private String language;
    private List<TranslatedSentenceItemDto> recordedSentences;
    private List<TranslatedSentenceItemDto> unrecordedSentences;
}
