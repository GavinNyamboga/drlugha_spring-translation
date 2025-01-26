package drlugha.translator.system.batch.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import drlugha.translator.system.language.dto.LanguageDTO;
import drlugha.translator.system.batch.model.BatchEntity;
import lombok.Data;

@Data
public class BatchResponseDTO {
    private Long batchNo;

    private String source;

    private String linkUrl;

    private String description;

    private Integer numberOfSentences;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LanguageDTO audioLanguage;

    public BatchResponseDTO(BatchEntity batchEntity) {
        batchNo = batchEntity.getBatchNo();
        source = batchEntity.getSource();
        linkUrl = batchEntity.getLinkUrl();
        description = batchEntity.getDescription();
        numberOfSentences = batchEntity.getSentences().size();
        if (batchEntity.getAudioLanguage() != null) {
            LanguageDTO languageDTO = new LanguageDTO();
            languageDTO.setLanguageId(batchEntity.getAudioLanguage().getLanguageId());
            languageDTO.setLanguageName(batchEntity.getAudioLanguage().getName());
            audioLanguage = languageDTO;
        }
    }

}
