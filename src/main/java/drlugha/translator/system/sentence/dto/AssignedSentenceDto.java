package drlugha.translator.system.sentence.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import drlugha.translator.system.sentence.model.Sentence;
import lombok.Data;

@Data
public class AssignedSentenceDto {
    private Long sentenceId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sentenceText;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String audioUrl;

    public AssignedSentenceDto(Sentence sentence) {
        sentenceId = sentence.getSentenceId();
        sentenceText = sentence.getSentenceText();
        audioUrl = sentence.getAudioLink();
    }
}
