package drlugha.translator.system.stats.dto;

import drlugha.translator.system.batch.enums.BatchType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LanguageStatisticsDTO {
    private String languageName;
    private Long languageId;
    private Integer totalBatchSentencesOrAudios;
    private Integer totalTranslated;
    private Integer totalTextApproved;
    private Integer totalTextRejected;
    private Integer totalTextExpertApproved;
    private Integer totalTextExpertRejected;
    private Integer totalAudioRecorded;
    private Integer totalAudioApproved;
    private Integer totalAudioRejected;
    private Integer totalAudioExpertApproved;
    private Integer totalExpertAudioRejected;
    private Long batchCount;
    private BatchType batchType;
    private List<Batch> batches;
    private List<BatchTypeStatistics> batchTypeStatistics = new ArrayList<>();

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchTypeStatistics {
        private Long batchCount;
        private BatchType batchType;
        private Integer totalBatchSentencesOrAudios;
        private Integer totalTranslated;
        private Integer totalTextApproved;
        private Integer totalTextRejected;
        private Integer totalTextExpertApproved;
        private Integer totalTextExpertRejected;
        private Integer textToBeExpertReviewed;
        private Integer totalAudioRecorded;
        private Integer totalAudioApproved;
        private Integer totalAudioRejected;
        private Integer totalAudioExpertApproved;
        private Integer totalExpertAudioRejected;
        private Integer audioToBeExpertReviewed;
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Batch {
        private String source;
        private String status;
        private String type;
        private Integer sentences;
        private TranslationDetails translationDetails;
        private List<AudioDetails> audioDetails;
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TranslationDetails {
        private String translator;
        private Integer translated;
        private Integer approved;
        private Integer rejected;
        private Integer expertApproved;
        private Integer expertRejected;
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AudioDetails {
        private String recorder;
        private Integer recorded;
        private Integer approved;
        private Integer rejected;
        private Integer expertApproved;
        private Integer expertRejected;
    }
}
