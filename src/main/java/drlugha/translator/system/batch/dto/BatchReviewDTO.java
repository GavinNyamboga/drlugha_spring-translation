package drlugha.translator.system.batch.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchReviewDTO {
    private String batch;
    private Integer row;
    private String username;
    private String englishSentence;
    private String datasetSentence; //translatedSentence
    private String modelSentence;
}
