package kmusau.translator.DTOs.sentenceDTOs;

import kmusau.translator.DTOs.userDTOs.UserDetailDto;
import kmusau.translator.entity.TranslatedSentenceEntity;
import kmusau.translator.entity.VoiceEntity;
import lombok.*;

@Data
@AllArgsConstructor
public class CompletedSentenceItemDto {
    private Long sentenceId;
    private String sentenceText;
    private Long translatedSentenceId;
    private String translatedText;
    private String audioUrl;
    private String transcriptionAudioUrl;
    private UserDetailDto recordedBy;

    public CompletedSentenceItemDto(VoiceEntity voice, UserDetailDto recordedBy) {
        this.sentenceId = voice.getTranslatedSentence().getSentenceId();
        this.sentenceText = voice.getTranslatedSentence().getSentence().getSentenceText();
        this.translatedSentenceId = voice.getTranslatedSentenceId();
        this.translatedText = voice.getTranslatedSentence().getTranslatedText();
        this.audioUrl = voice.getFileUrl();
        this.recordedBy = recordedBy;
    }

    public CompletedSentenceItemDto(SentenceItemDto sentenceItemDto, UserDetailDto recordedBy) {
        this.sentenceId = sentenceItemDto.getSentenceId();
        this.sentenceText = sentenceItemDto.getSentenceText();
        this.translatedSentenceId = sentenceItemDto.getTranslatedSentenceId();
        this.translatedText = sentenceItemDto.getTranslatedText();
        this.audioUrl = sentenceItemDto.getAudioUrl();
        this.transcriptionAudioUrl = sentenceItemDto.getTranscriptionAudioUrl();
        this.recordedBy = recordedBy;
    }

    public CompletedSentenceItemDto(TranslatedSentenceEntity entity, UserDetailDto recordedBy) {
        this.sentenceId = entity.getSentenceId();
        this.sentenceText = entity.getSentence().getSentenceText();
        this.translatedSentenceId = entity.getTranslatedSentenceId();
        this.translatedText = entity.getTranslatedText();
        this.recordedBy = recordedBy;
    }

    public String getAudioUrl() {
        return audioUrl != null && !audioUrl.isBlank() ? audioUrl : null;
    }
}

