package kmusau.translator.projections;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface ExpertStats {
    @JsonIgnore
    Long getUserId();
    Integer getSentencesExpertApproved();
    Integer getSentencesExpertRejected();
}
