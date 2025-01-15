package drlugha.translator.DTOs.languageDTOs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import drlugha.translator.entity.LanguageEntity;
import drlugha.translator.enums.DeletionStatus;
import lombok.Data;

@Data
public class LanguageDTO {
    private Long languageId;
    private String languageName;

    @JsonIgnore
    public LanguageEntity getEntity() {
        LanguageEntity languageEntity = new LanguageEntity();
        languageEntity.setName(languageName);
        languageEntity.setDeletionStatus(DeletionStatus.NOT_DELETED);
        return languageEntity;
    }
}
