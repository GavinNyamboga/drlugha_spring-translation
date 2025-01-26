package drlugha.translator.system.batch.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackDTO {
    private String sourceText;
    private String translatedText;
    private String editedTranslatedText;
    private String sourceLanguage; //language name
    private Long sourceLanguageId;
    private String targetLanguage; //language name
    private Long targetLanguageId;
    private String rating;
    private Integer score;
}
