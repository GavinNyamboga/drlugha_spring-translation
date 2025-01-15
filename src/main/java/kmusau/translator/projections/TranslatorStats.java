package kmusau.translator.projections;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface TranslatorStats {
    @JsonIgnore
    Long getUserId();
    Integer getSentencesTranslated();
    Integer getSentencesApproved();
    Integer getSentencesRejected();
}
