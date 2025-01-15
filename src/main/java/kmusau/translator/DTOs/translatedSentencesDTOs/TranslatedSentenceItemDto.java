package kmusau.translator.DTOs.translatedSentencesDTOs;

import kmusau.translator.entity.TranslatedSentenceEntity;
import kmusau.translator.entity.VoiceEntity;
import lombok.Data;

@Data
public class TranslatedSentenceItemDto {
    private Long translatedSentenceId;
    private String translatedSentenceText;
    private Long sentenceId;
    private String sentenceText;
    private Boolean accepted;

    private String comment;

    private String audioLink;

    private Long voiceId;

    public static TranslatedSentenceItemDto entityToDto(TranslatedSentenceEntity translatedSentenceEntity, String comments, Boolean isAccepted){
        TranslatedSentenceItemDto translatedSentenceItemDto = new TranslatedSentenceItemDto();
        translatedSentenceItemDto.translatedSentenceId = translatedSentenceEntity.getTranslatedSentenceId();
        translatedSentenceItemDto.translatedSentenceText = translatedSentenceEntity.getTranslatedText();
        translatedSentenceItemDto.sentenceId = translatedSentenceEntity.getSentenceId();
        translatedSentenceItemDto.sentenceText = translatedSentenceEntity.getSentence().getSentenceText();
        translatedSentenceItemDto.audioLink = translatedSentenceEntity.getSentence().getAudioLink();
        translatedSentenceItemDto.accepted = isAccepted;
        if (isAccepted != null && !isAccepted) {
            translatedSentenceItemDto.comment = comments;
        }
        return translatedSentenceItemDto;
    }

    public static TranslatedSentenceItemDto voiceEntityToDto(VoiceEntity voice, String comments, Boolean isAccepted){
        TranslatedSentenceItemDto translatedSentenceItemDto =
                entityToDto(voice.getTranslatedSentence(), comments, isAccepted);
        translatedSentenceItemDto.audioLink = voice.getPresignedUrl();
        translatedSentenceItemDto.voiceId = voice.getVoiceId();
        return translatedSentenceItemDto;
    }
}
