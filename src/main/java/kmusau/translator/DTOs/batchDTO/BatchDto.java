package kmusau.translator.DTOs.batchDTO;

import kmusau.translator.DTOs.sentenceDTOs.CreateSentenceDto;
import kmusau.translator.entity.BatchEntity;
import kmusau.translator.enums.DeletionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BatchDto {
    
    private Long batchNo;

    private String source;

    private String linkUrl;

    private String description;

    private Long uploaderId;

    private List<CreateSentenceDto> sentences;

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
