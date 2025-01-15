package kmusau.translator.DTOs.sentenceDTOs;

import kmusau.translator.entity.SentenceEntity;
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

    private List<SentenceEntity> sentences;
    private List<AssignedSentenceDto> pendingTasks;

}
