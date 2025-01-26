package drlugha.translator.system.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchInfoDTO {
    private List<BatchInfoItemDTO> translationAssignments;

    private List<BatchInfoItemDTO> transcriptionAssignments;
}
