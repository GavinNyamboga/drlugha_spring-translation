package drlugha.translator.DTOs.sentenceDTOs;

import drlugha.translator.entity.SentenceEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSentenceDTO {

    private String sentenceText;

    private String language;

    public SentenceEntity dtoToEntity(CreateSentenceDTO sentenceDto) {
        SentenceEntity sentenceEntity = new SentenceEntity();
        if (sentenceDto == null) {
            return sentenceEntity;
        }

        if (sentenceDto.getSentenceText() != null)
            sentenceEntity.setSentenceText(sentenceDto.getSentenceText());

        return sentenceEntity;
    }
}
