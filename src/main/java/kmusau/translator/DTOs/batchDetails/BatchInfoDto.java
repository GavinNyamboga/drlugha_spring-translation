package kmusau.translator.DTOs.batchDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchInfoDto {
    private List<BatchInfoItemDTO> translationAssignments;

    private List<BatchInfoItemDTO> transcriptionAssignments;
}
