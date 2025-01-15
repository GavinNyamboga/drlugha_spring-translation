package kmusau.translator.DTOs.translatedSentencesDTOs;

import lombok.Data;

@Data
public class RejectTranslationDto {
    private Long translatedSentenceId;
    private String comment;
}
