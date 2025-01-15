package kmusau.translator.DTOs.batchDetails;

import kmusau.translator.entity.BatchDetailsEntity;
import kmusau.translator.enums.BatchStatus;
import lombok.Data;

@Data
public class BatchInfoItemDTO {
    private Long batchDetailsId;
    private Long batchNo;
    private String batchSource;
    private String batchDescription;
    private String batchLink;

    private String language;

    private Integer pendingSentences;
    private Boolean translated;
    private Boolean reviewed;
    private Boolean expertReviewed;
    private Boolean audioRecorded;
    private Boolean audioReviewed;

    public BatchInfoItemDTO(BatchDetailsEntity batchDetailsEntity){
        batchDetailsId = batchDetailsEntity.getBatchDetailsId();
        batchNo = batchDetailsEntity.getBatch().getBatchNo();
        batchSource = batchDetailsEntity.getBatch().getSource();
        batchDescription = batchDetailsEntity.getBatch().getDescription();
        batchLink = batchDetailsEntity.getBatch().getLinkUrl();
        language = batchDetailsEntity.getLanguage().getName();
        translated = batchDetailsEntity.getBatchStatus().ordinal() > BatchStatus.assignedTranslator.ordinal();
        reviewed = batchDetailsEntity.getBatchStatus().ordinal() > BatchStatus.assignedTextVerifier.ordinal();
        expertReviewed = batchDetailsEntity.getBatchStatus().ordinal() > BatchStatus.assignedExpertReviewer.ordinal();
        audioRecorded = batchDetailsEntity.getBatchStatus().ordinal() > BatchStatus.assignedRecorder.ordinal();
        audioReviewed = batchDetailsEntity.getBatchStatus().ordinal() > BatchStatus.assignedAudioVerifier.ordinal();
    }
}
