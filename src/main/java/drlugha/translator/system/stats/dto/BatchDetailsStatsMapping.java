package drlugha.translator.system.stats.dto;

public interface BatchDetailsStatsMapping {

    Long getBatchDetailsId();

    Long getBatchNo();

    String getSource();

    String getLanguage();

    String getStatus();

    Integer getNumberOfSentences();

    Integer getSentencesTranslated();

    Integer getSentencesApproved();

    Integer getSentencesRejected();

    Integer getSentencesExpertApproved();

    Integer getSentencesExpertRejected();

    Integer getAudiosRecorded();

    Integer getAudiosApproved();

    Integer getAudiosRejected();

    String getTranslator();

    String getModerator();

    String getExpert();

    String getRecorder();

    String getAudioModerator();

}
