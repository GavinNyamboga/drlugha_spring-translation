package kmusau.translator.DTOs.batchDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import kmusau.translator.DTOs.languageDTOs.LanguageDTO;
import kmusau.translator.entity.BatchEntity;
import lombok.Data;

@Data
public class BatchResponseDto {
    private Long batchNo;

    private String source;

    private String linkUrl;

    private String description;

    private Integer numberOfSentences;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LanguageDTO audioLanguage;

    public BatchResponseDto(BatchEntity batchEntity){
        batchNo = batchEntity.getBatchNo();
        source = batchEntity.getSource();
        linkUrl = batchEntity.getLinkUrl();
        description = batchEntity.getDescription();
        numberOfSentences = batchEntity.getSentences().size();
        if (batchEntity.getAudioLanguage() != null){
            LanguageDTO languageDTO = new LanguageDTO();
            languageDTO.setLanguageId(batchEntity.getAudioLanguage().getLanguageId());
            languageDTO.setLanguageName(batchEntity.getAudioLanguage().getName());
            audioLanguage = languageDTO;
        }
    }

}
