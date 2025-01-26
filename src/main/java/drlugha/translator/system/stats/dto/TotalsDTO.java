package drlugha.translator.system.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TotalsDTO {
    private Long totalSentences;

    private Long totalAudios;

    private Long totalTranslatedSentences;

    private Long totalTranscribedAudios;

    public TotalsDTO(TotalSentencesDto totalSentencesDto, TotalTranslatedSentencesDto totalTranslatedSentencesDto) {
        this.totalSentences = totalSentencesDto.getTotalSentences();
        this.totalAudios = totalSentencesDto.getTotalUploadedAudios();
        this.totalTranslatedSentences = totalTranslatedSentencesDto.getTotalTranslatedSentences();
        this.totalTranscribedAudios = totalTranslatedSentencesDto.getTotalTranscribedAudios();
    }
}
