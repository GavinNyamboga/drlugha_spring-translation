package kmusau.translator.DTOs.stats;

public interface TotalUserStatsDto {
    long getUserId();
    String getUsername();
    int getSentencesTranslated();
    int getSentencesModerated();
    int getSentencesExpertModerated();
    int getAudiosRecorded();
    int getAudioModerated();
}
