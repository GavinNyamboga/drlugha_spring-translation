package drlugha.translator.system.stats.dto;

public interface TotalUserStatsDto {
    long getUserId();

    String getUsername();

    int getSentencesTranslated();

    int getSentencesModerated();

    int getSentencesExpertModerated();

    int getAudiosRecorded();

    int getAudioModerated();
}
