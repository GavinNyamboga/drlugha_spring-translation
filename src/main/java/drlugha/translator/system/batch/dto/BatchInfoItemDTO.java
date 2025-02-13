package drlugha.translator.system.batch.dto;

import drlugha.translator.system.batch.model.BatchDetailsEntity;
import drlugha.translator.system.batch.enums.BatchStatus;
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
    private Boolean audioExpertReviewed;

    public BatchInfoItemDTO(BatchDetailsEntity batchDetailsEntity) {
        batchDetailsId = batchDetailsEntity.getBatchDetailsId();
        batchNo = batchDetailsEntity.getBatch().getBatchNo();
        batchSource = batchDetailsEntity.getBatch().getSource();
        batchDescription = batchDetailsEntity.getBatch().getDescription();
        batchLink = batchDetailsEntity.getBatch().getLinkUrl();
        language = batchDetailsEntity.getLanguage().getName();
        translated = batchDetailsEntity.getBatchStatus().ordinal() > BatchStatus.ASSIGNED_TRANSLATOR.ordinal();
        reviewed = batchDetailsEntity.getBatchStatus().ordinal() > BatchStatus.ASSIGNED_TEXT_VERIFIER.ordinal();
        expertReviewed = batchDetailsEntity.getBatchStatus().ordinal() > BatchStatus.ASSIGNED_EXPERT_REVIEWER.ordinal();
        audioRecorded = batchDetailsEntity.getBatchStatus().ordinal() > BatchStatus.ASSIGNED_RECORDER.ordinal();
        audioReviewed = batchDetailsEntity.getBatchStatus().ordinal() > BatchStatus.ASSIGNED_AUDIO_VERIFIER.ordinal();
        audioExpertReviewed = batchDetailsEntity.getBatchStatus().ordinal() > BatchStatus.ASSIGNED_EXPERT_AUDIO_REVIEWER.ordinal();
    }
}
