package drlugha.translator.system.sentence.dto;


public interface SentenceItemDto {
    Long getSentenceId();

    String getSentenceText();

    String getTranscriptionAudioUrl();

    Long getTranslatedSentenceId();

    String getTranslatedText();

    String getAudioUrl();

    Long getRecordedByUserId();

    String getRecordedByUsername();

    Long getBatchDetailsId(); // Add this method to the interface
}
