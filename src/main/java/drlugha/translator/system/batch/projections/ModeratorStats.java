package drlugha.translator.system.batch.projections;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface ModeratorStats {
    @JsonIgnore
    Long getUserId();

    Integer getSentencesApproved();

    Integer getSentencesRejected();
}
