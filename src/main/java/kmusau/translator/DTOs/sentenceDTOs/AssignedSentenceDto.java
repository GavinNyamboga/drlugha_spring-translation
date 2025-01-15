package kmusau.translator.DTOs.sentenceDTOs;

import com.fasterxml.jackson.annotation.JsonInclude;
import kmusau.translator.entity.SentenceEntity;
import lombok.Data;

@Data
public class AssignedSentenceDto {
    private Long sentenceId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sentenceText;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String audioUrl;

    public AssignedSentenceDto(SentenceEntity sentenceEntity){
        sentenceId = sentenceEntity.getSentenceId();
        sentenceText = sentenceEntity.getSentenceText();
        audioUrl = sentenceEntity.getAudioLink();
    }
}
