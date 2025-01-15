package kmusau.translator.DTOs.sentenceDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ExpertReviewedSentencesDto {
    private String language;
    private List<CompletedSentenceItemDto> expertReviewedSentences;
}
