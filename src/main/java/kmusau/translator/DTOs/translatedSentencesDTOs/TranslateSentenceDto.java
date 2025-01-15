package kmusau.translator.DTOs.translatedSentencesDTOs;

import kmusau.translator.entity.TranslatedSentenceEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TranslateSentenceDto {

    private String translatedText;

    private Long batchDetailsId;

    public TranslatedSentenceEntity dtoToEntity(TranslateSentenceDto translateSentenceDto) {
        TranslatedSentenceEntity translatedSentence = new TranslatedSentenceEntity();
        if (translateSentenceDto == null) {
            return translatedSentence;
        }

        if (translateSentenceDto.getTranslatedText() != null)
            translatedSentence.setTranslatedText(translateSentenceDto.getTranslatedText());

        return translatedSentence;
    }

    public TranslateSentenceDto entityToDto(TranslatedSentenceEntity translatedSentenceEntity) {
        TranslateSentenceDto translateSentenceDto = new TranslateSentenceDto();
        if (translatedSentenceEntity == null)  {
            return translateSentenceDto;
        }

        if (translatedSentenceEntity.getTranslatedText() != null)
            translateSentenceDto.setTranslatedText(translatedSentenceEntity.getTranslatedText());

        return translateSentenceDto;
    }
}
