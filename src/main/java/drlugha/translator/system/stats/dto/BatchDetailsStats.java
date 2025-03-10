package drlugha.translator.system.stats.dto;

import drlugha.translator.system.batch.model.BatchDetailsEntity;
import drlugha.translator.system.batch.model.BatchDetailsStatsEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BatchDetailsStats {
    private Long batchDetailsId;
    private Long batchNo;
    private String source;
    private String language;
    private String status;

    private Integer numberOfSentences;
    private Integer sentencesTranslated;
    private Integer sentencesApproved;
    private Integer sentencesRejected;
    private Integer sentencesExpertApproved;
    private Integer sentencesExpertRejected;
    private Integer audiosRecorded;
    private Integer audiosApproved;
    private Integer audiosRejected;
    private String translator;
    private String moderator;
    private String expert;
    private String recorder;
    private String audioModerator;
    private String audioExpertReviewer;

    private List<AudioStats> audioStats = new ArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AudioStats {
        private String recorder;
        private Integer audiosRecorded;
        private Integer audiosApproved;
        private Integer audiosRejected;
        private Integer audiosExpertApproved;
        private Integer audiosExpertRejected;
    }

    public static BatchDetailsStats entityToDto(BatchDetailsStatsEntity batchDetailsStatsEntity) {
        BatchDetailsStats batchDetailsStats = new BatchDetailsStats();
        BatchDetailsEntity batchDetails = batchDetailsStatsEntity.getBatchDetails();
        batchDetailsStats.batchDetailsId = batchDetails.getBatchDetailsId();
        batchDetailsStats.source = batchDetails.getBatch().getSource();
        batchDetailsStats.language = batchDetails.getLanguage().getName();
        batchDetailsStats.status = batchDetails.getBatchStatus().getLabel(batchDetails.getBatch().getBatchType());

        if (batchDetails.getTranslatedBy() != null)
            batchDetailsStats.translator = batchDetails.getTranslatedBy().getUsername();

        batchDetailsStats.numberOfSentences = batchDetails.getBatch().getSentences().size();
        batchDetailsStats.sentencesTranslated = batchDetailsStatsEntity.getSentencesTranslated();

        if (batchDetails.getTranslationVerifiedBy() != null)
            batchDetailsStats.moderator = batchDetails.getTranslationVerifiedBy().getUsername();

        batchDetailsStats.sentencesApproved = batchDetailsStatsEntity.getSentencesApproved();
        batchDetailsStats.sentencesRejected = batchDetailsStatsEntity.getSentencesRejected();

        if (batchDetails.getSecondReviewer() != null)
            batchDetailsStats.expert = batchDetails.getSecondReviewer().getUsername();

        batchDetailsStats.sentencesExpertApproved = batchDetailsStatsEntity.getSentencesExpertApproved();
        batchDetailsStats.sentencesExpertRejected = batchDetailsStatsEntity.getSentencesExpertRejected();

        if (batchDetails.getRecordedBy() != null)
            batchDetailsStats.recorder = batchDetails.getRecordedBy().getUsername();

        batchDetailsStats.audiosRecorded = batchDetailsStatsEntity.getAudiosRecorded();

        if (batchDetails.getAudioVerifiedBy() != null)
            batchDetailsStats.audioModerator = batchDetails.getAudioVerifiedBy().getUsername();

        batchDetailsStats.audiosApproved = batchDetailsStatsEntity.getAudiosApproved();
        batchDetailsStats.audiosRejected = batchDetailsStatsEntity.getAudiosRejected();
        return batchDetailsStats;
    }
}
