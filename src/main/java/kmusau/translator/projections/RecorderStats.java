package kmusau.translator.projections;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface RecorderStats {
    @JsonIgnore
    Long getUserId();
    Integer getAudiosRecorded();
    Integer getAudiosApproved();
    Integer getAudiosRejected();
}
