package drlugha.translator.system.sentence.dto;

import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TranslateSentenceDTO {

    private String translatedText;

    private Long batchDetailsId;

    public TranslatedSentenceEntity dtoToEntity(TranslateSentenceDTO translateSentenceDto) {
        TranslatedSentenceEntity translatedSentence = new TranslatedSentenceEntity();
        if (translateSentenceDto == null) {
            return translatedSentence;
        }

        if (translateSentenceDto.getTranslatedText() != null)
            translatedSentence.setTranslatedText(translateSentenceDto.getTranslatedText());

        return translatedSentence;
    }

    public TranslateSentenceDTO entityToDto(TranslatedSentenceEntity translatedSentenceEntity) {
        TranslateSentenceDTO translateSentenceDto = new TranslateSentenceDTO();
        if (translatedSentenceEntity == null) {
            return translateSentenceDto;
        }

        if (translatedSentenceEntity.getTranslatedText() != null)
            translateSentenceDto.setTranslatedText(translatedSentenceEntity.getTranslatedText());

        return translateSentenceDto;
    }
}
