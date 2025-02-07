package drlugha.translator.system.sentence.dto;

import drlugha.translator.system.sentence.model.Sentence;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSentenceDTO {

    private String sentenceText;

    private String language;

    public Sentence dtoToEntity(CreateSentenceDTO sentenceDto) {
        Sentence sentence = new Sentence();
        if (sentenceDto == null) {
            return sentence;
        }

        if (sentenceDto.getSentenceText() != null)
            sentence.setSentenceText(sentenceDto.getSentenceText());

        return sentence;
    }
}
