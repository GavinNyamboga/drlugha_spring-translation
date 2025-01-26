package drlugha.translator.system.batch.dto;

import drlugha.translator.system.batch.enums.UserBatchRole;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BatchUserAssignmentDTO {

    private UserBatchRole role;

    private List<Long> userIds = new ArrayList<>();

    private Long userId;
}
