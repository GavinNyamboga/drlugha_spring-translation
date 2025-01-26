package drlugha.translator.system.batch.dto;

import drlugha.translator.system.stats.dto.BatchDetailsStatsDto;
import drlugha.translator.system.batch.model.BatchDetailsEntity;
import lombok.Data;

@Data
public class BatchInfoStatsDTO {
    private Long batchDetailsId;
    private Long batchNo;
    private String batchSource;
    private String batchDescription;
    private String batchLink;

    private String batchDetailsStatus;
    private String language;

    Integer numberOfAllSentences;
    Long moderatorApprovedSentences;
    Long moderatorRejectedSentences;
    Long expertApprovedSentences;
    Long expertRejectedSentences;

    public static BatchInfoStatsDTO entityToDto(BatchDetailsEntity batchDetailsEntity, Integer numberOfAllSentences, BatchDetailsStatsDto batchDetailsStatsDto) {
        BatchInfoStatsDTO batchInfoStatsDto = new BatchInfoStatsDTO();
        batchInfoStatsDto.batchDetailsId = batchDetailsEntity.getBatchDetailsId();
        batchInfoStatsDto.batchNo = batchDetailsEntity.getBatch().getBatchNo();
        batchInfoStatsDto.batchSource = batchDetailsEntity.getBatch().getSource();
        batchInfoStatsDto.batchDescription = batchDetailsEntity.getBatch().getDescription();
        batchInfoStatsDto.batchLink = batchDetailsEntity.getBatch().getLinkUrl();
        batchInfoStatsDto.language = batchDetailsEntity.getLanguage().getName();
        batchInfoStatsDto.numberOfAllSentences = numberOfAllSentences;
        batchInfoStatsDto.moderatorApprovedSentences = batchDetailsStatsDto.getModeratorApprovedSentences();
        batchInfoStatsDto.moderatorRejectedSentences = batchDetailsStatsDto.getModeratorRejectedSentences();
        batchInfoStatsDto.expertApprovedSentences = batchDetailsStatsDto.getExpertApprovedSentences();
        batchInfoStatsDto.expertRejectedSentences = batchDetailsStatsDto.getExpertRejectedSentences();
        batchInfoStatsDto.batchDetailsStatus = batchDetailsEntity.getBatchStatus().getLabel(batchDetailsEntity.getBatch().getBatchType());

        return batchInfoStatsDto;
    }
}
