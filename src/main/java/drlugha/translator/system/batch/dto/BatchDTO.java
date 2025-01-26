package drlugha.translator.system.batch.dto;

import drlugha.translator.system.sentence.dto.CreateSentenceDTO;
import drlugha.translator.system.batch.model.BatchEntity;
import drlugha.translator.shared.enums.DeletionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BatchDTO {

    private Long batchNo;

    private String source;

    private String linkUrl;

    private String description;

    private Long uploaderId;

    private List<CreateSentenceDTO> sentences;

    public BatchEntity dtoToEntity() {
        BatchEntity batchEntity = new BatchEntity();

        if (this.getBatchNo() != null)
            batchEntity.setBatchNo(this.getBatchNo());
        if (this.getSource() != null)
            batchEntity.setSource(this.getSource());
        if (this.getLinkUrl() != null)
            batchEntity.setLinkUrl(this.getLinkUrl());
        if (this.getDescription() != null)
            batchEntity.setDescription(this.getDescription());
        if (this.getUploaderId() != null)
            batchEntity.setUploaderId(this.getUploaderId());
        batchEntity.setDeletionStatus(DeletionStatus.NOT_DELETED);

        return batchEntity;

    }
}
