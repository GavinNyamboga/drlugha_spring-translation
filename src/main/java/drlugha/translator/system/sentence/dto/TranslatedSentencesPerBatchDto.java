package drlugha.translator.system.sentence.dto;

import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.shared.enums.StatusTypes;
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
                translatedSentenceEntity.getReviewStatus() != StatusTypes.UNREVIEWED) {
            translatedSentencesPerBatchDto.approved = translatedSentenceEntity.getReviewStatus() == StatusTypes.APPROVED;
        }

        if (translatedSentenceEntity.getSecondReview() != null &&
                translatedSentenceEntity.getReviewStatus() != StatusTypes.UNREVIEWED &&
                translatedSentenceEntity.getSecondReview() != StatusTypes.UNREVIEWED) {
            translatedSentencesPerBatchDto.approved = translatedSentenceEntity.getSecondReview() == StatusTypes.APPROVED;
        }

        translatedSentencesPerBatchDto.moderatorComment = moderatorComments;
        translatedSentencesPerBatchDto.expertComment = expertComments;

        return translatedSentencesPerBatchDto;
    }

}
