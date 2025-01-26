package drlugha.translator.system.sentence.dto;

import lombok.Data;

@Data
public class RejectTranslationDto {
    private Long translatedSentenceId;
    private String comment;
}
