package drlugha.translator.system.stats.dto;

import drlugha.translator.system.batch.enums.BatchType;

public interface LanguageStats {

    String getLanguageName();

    Long getLanguageId();

    Integer getTotalBatchSentencesOrAudios();

    Integer getTotalTranslated();

    Integer getTotalTextApproved();

    Integer getTotalTextRejected();

    Integer getTotalTextExpertApproved();

    Integer getTotalTextExpertRejected();

    Integer getTotalAudioRecorded();

    Integer getTotalAudioApproved();

    Integer getTotalAudioRejected();

    Integer getTotalAudioExpertApproved();

    Integer getTotalExpertAudioRejected();

    Long getBatchCount();

    BatchType getBatchType();
}
