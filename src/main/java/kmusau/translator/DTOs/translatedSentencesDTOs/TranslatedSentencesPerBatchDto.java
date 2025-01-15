package kmusau.translator.DTOs.translatedSentencesDTOs;

import kmusau.translator.DTOs.sentenceDTOs.AssignedSentenceDto;
import kmusau.translator.entity.SentenceEntity;
import kmusau.translator.entity.TranslatedSentenceEntity;
import kmusau.translator.enums.StatusTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TranslatedSentencesPerBatchDto {

    private long translatedSentenceId;

    private String translatedText;

    private AssignedSentenceDto sentence;

    private Boolean approved;

    private long batchDetailsId;

    private String moderatorComment;

    private String expertComment;

    public TranslatedSentencesPerBatchDto toDto(
            TranslatedSentenceEntity translatedSentenceEntity,
            String moderatorComments,
            String expertComments
    ) {
        TranslatedSentencesPerBatchDto translatedSentencesPerBatchDto = new TranslatedSentencesPerBatchDto();

        if (translatedSentenceEntity == null) {
            return new TranslatedSentencesPerBatchDto();
        }

        if (translatedSentenceEntity.getTranslatedSentenceId() != null) {
            translatedSentencesPerBatchDto.setTranslatedSentenceId(translatedSentenceEntity.getTranslatedSentenceId());
        }
        if (translatedSentenceEntity.getTranslatedText() != null) {
            translatedSentencesPerBatchDto.setTranslatedText(translatedSentenceEntity.getTranslatedText());
        }
        if (translatedSentenceEntity.getSentence() != null) {
            translatedSentencesPerBatchDto.setSentence(new AssignedSentenceDto(translatedSentenceEntity.getSentence()));
        }
        if (translatedSentenceEntity.getBatchDetailsId() != null) {
            translatedSentencesPerBatchDto.setBatchDetailsId(translatedSentenceEntity.getBatchDetailsId());
        }

        if (translatedSentenceEntity.getReviewStatus() != null &&
                translatedSentenceEntity.getReviewStatus() != StatusTypes.unreviewed){
            translatedSentencesPerBatchDto.approved = translatedSentenceEntity.getReviewStatus() == StatusTypes.approved;
        }

        if (translatedSentenceEntity.getSecondReview() != null &&
                translatedSentenceEntity.getReviewStatus() != StatusTypes.unreviewed &&
                translatedSentenceEntity.getSecondReview() != StatusTypes.unreviewed){
            translatedSentencesPerBatchDto.approved = translatedSentenceEntity.getSecondReview() == StatusTypes.approved;
        }

        translatedSentencesPerBatchDto.moderatorComment = moderatorComments;
        translatedSentencesPerBatchDto.expertComment = expertComments;

        return translatedSentencesPerBatchDto;
    }

}
