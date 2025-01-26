package drlugha.translator.system.sentence.dto;

import drlugha.translator.system.sentence.model.Sentence;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SentencesToTranslateDto {

    private Long batchDetailsId;
    private String language;

    private String batchType;

    private List<Sentence> sentences;
    private List<AssignedSentenceDto> pendingTasks;

}
