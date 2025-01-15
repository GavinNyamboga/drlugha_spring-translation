package kmusau.translator.DTOs.sentenceDTOs;

import kmusau.translator.entity.SentenceEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSentenceDto {

    private String sentenceText;

    private String language;

    public SentenceEntity dtoToEntity(CreateSentenceDto sentenceDto) {
        SentenceEntity sentenceEntity = new SentenceEntity();
        if (sentenceDto == null) {
            return sentenceEntity;
        }

        if (sentenceDto.getSentenceText() != null)
            sentenceEntity.setSentenceText(sentenceDto.getSentenceText());

        return sentenceEntity;
    }
}
