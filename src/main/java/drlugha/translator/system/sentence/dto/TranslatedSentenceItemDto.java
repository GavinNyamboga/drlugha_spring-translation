package drlugha.translator.system.sentence.dto;

import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.system.user.enums.Gender;
import drlugha.translator.system.voice.model.VoiceEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TranslatedSentenceItemDto {
    private Long translatedSentenceId;
    private String translatedSentenceText;
    private Long sentenceId;
    private String sentenceText;
    private Boolean accepted;

    private String comment;

    private String audioLink;

    private Long voiceId;

    private List<AudioDTO> audioList = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AudioDTO {
        private Long voiceId;
        private String audioLink;
        private String recordedBy;
        private Gender gender;
        private Boolean accepted;
        private String comment;
    }

    public static TranslatedSentenceItemDto entityToDto(TranslatedSentenceEntity translatedSentenceEntity, String comments, Boolean isAccepted) {
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

    public static TranslatedSentenceItemDto voiceEntityToDto(VoiceEntity voice, String comments, Boolean isAccepted) {
        TranslatedSentenceItemDto translatedSentenceItemDto = entityToDto(voice.getTranslatedSentence(), comments, isAccepted);
        translatedSentenceItemDto.audioLink = voice.getPresignedUrl();
        translatedSentenceItemDto.voiceId = voice.getVoiceId();

        AudioDTO audioDTO = new AudioDTO();
        audioDTO.setAudioLink(voice.getPresignedUrl());
        audioDTO.setRecordedBy(voice.getUser() != null ? voice.getUser().getUsername() : null);
        audioDTO.setVoiceId(voice.getVoiceId());
        audioDTO.setAccepted(isAccepted);

        translatedSentenceItemDto.audioList.add(audioDTO);
        return translatedSentenceItemDto;
    }
}
