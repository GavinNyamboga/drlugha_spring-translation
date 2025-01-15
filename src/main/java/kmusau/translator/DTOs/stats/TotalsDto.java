package kmusau.translator.DTOs.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TotalsDto {
    private Long totalSentences;

    private Long totalAudios;

    private Long totalTranslatedSentences;

    private Long totalTranscribedAudios;

    public TotalsDto(TotalSentencesDto totalSentencesDto, TotalTranslatedSentencesDto totalTranslatedSentencesDto) {
        this.totalSentences = totalSentencesDto.getTotalSentences();
        this.totalAudios = totalSentencesDto.getTotalUploadedAudios();
        this.totalTranslatedSentences = totalTranslatedSentencesDto.getTotalTranslatedSentences();
        this.totalTranscribedAudios = totalTranslatedSentencesDto.getTotalTranscribedAudios();
    }
}
