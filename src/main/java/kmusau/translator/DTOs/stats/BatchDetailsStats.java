package kmusau.translator.DTOs.stats;

import kmusau.translator.entity.BatchDetailsEntity;
import kmusau.translator.entity.BatchDetailsStatsEntity;
import lombok.Data;

@Data
public class BatchDetailsStats {
    private Long batchDetailsId;
    private String source;

    private String language;

    private String status;
    private String translator;
    private int numberOfSentences;
    private int sentencesTranslated;
    private String moderator;
    private int sentencesApproved;
    private int sentencesRejected;
    private String expert;
    private int sentencesExpertApproved;
    private int sentencesExpertRejected;
    private String recorder;
    private int audiosRecorded;
    private String audioModerator;
    private int audiosApproved;
    private int audiosRejected;

    public static BatchDetailsStats entityToDto(BatchDetailsStatsEntity batchDetailsStatsEntity){
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
