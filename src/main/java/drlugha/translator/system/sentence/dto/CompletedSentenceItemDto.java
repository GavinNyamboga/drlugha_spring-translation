package drlugha.translator.system.sentence.dto;

import drlugha.translator.system.user.dto.UserDetailDTO;
import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.system.voice.model.VoiceEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompletedSentenceItemDto {
    private Long batchDetailsId;
    private Long sentenceId;
    private String sentenceText;
    private Long translatedSentenceId;
    private String translatedText;
    private String audioUrl;
    private String transcriptionAudioUrl;
    private UserDetailDTO recordedBy;
    private List<UserDetailDTO> audioDetails = new ArrayList<>();

    public CompletedSentenceItemDto(VoiceEntity voice, UserDetailDTO recordedBy) {
        this.sentenceId = voice.getTranslatedSentence().getSentenceId();
        this.sentenceText = voice.getTranslatedSentence().getSentence().getSentenceText();
        this.translatedSentenceId = voice.getTranslatedSentenceId();
        this.translatedText = voice.getTranslatedSentence().getTranslatedText();
        this.audioUrl = voice.getFileUrl();
        this.recordedBy = recordedBy;
    }

    public CompletedSentenceItemDto(SentenceItemDto sentenceItemDto, UserDetailDTO recordedBy) {
        this.sentenceId = sentenceItemDto.getSentenceId();
        this.sentenceText = sentenceItemDto.getSentenceText();
        this.translatedSentenceId = sentenceItemDto.getTranslatedSentenceId();
        this.translatedText = sentenceItemDto.getTranslatedText();
        this.audioUrl = sentenceItemDto.getAudioUrl();
        this.transcriptionAudioUrl = sentenceItemDto.getTranscriptionAudioUrl();
        this.recordedBy = recordedBy;
        this.batchDetailsId = sentenceItemDto.getBatchDetailsId();
    }

    public CompletedSentenceItemDto(TranslatedSentenceEntity entity, UserDetailDTO recordedBy) {
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

