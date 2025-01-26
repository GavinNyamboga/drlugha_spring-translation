package drlugha.translator.system.batch.projections;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface ExpertStats {
    @JsonIgnore
    Long getUserId();

    Integer getSentencesExpertApproved();

    Integer getSentencesExpertRejected();
}
