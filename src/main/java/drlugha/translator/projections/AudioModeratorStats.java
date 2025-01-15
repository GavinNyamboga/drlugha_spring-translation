package drlugha.translator.projections;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface AudioModeratorStats {
    @JsonIgnore
    Long getUserId();

    Integer getAudiosApproved();

    Integer getAudiosRejected();
}
