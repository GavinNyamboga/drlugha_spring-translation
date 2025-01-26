package drlugha.translator.system.language.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import drlugha.translator.system.language.model.Language;
import drlugha.translator.shared.enums.DeletionStatus;
import lombok.Data;

@Data
public class LanguageDTO {
    private Long languageId;
    private String languageName;

    @JsonIgnore
    public Language getEntity() {
        Language language = new Language();
        language.setName(languageName);
        language.setDeletionStatus(DeletionStatus.NOT_DELETED);
        return language;
    }
}
