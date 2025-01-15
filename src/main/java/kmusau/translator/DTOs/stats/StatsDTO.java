package kmusau.translator.DTOs.stats;

public interface StatsDTO {
    Long getUserId();
    String getUsername();
    String getEmail();
    Long getTranslated();
    Long getTranslationsVerified();
    Long getTranslationsExpertVerified();
    Long getRecorded();
    Long getAudiosVerified();

    Long getAudiosRejected();
}
