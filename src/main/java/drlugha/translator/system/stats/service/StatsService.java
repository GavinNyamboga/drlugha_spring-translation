package drlugha.translator.system.stats.service;

import drlugha.translator.system.stats.dto.LanguageStatisticsDTO;
import drlugha.translator.system.stats.dto.LanguageStats;
import drlugha.translator.system.stats.dto.StatsDTO;
import drlugha.translator.system.stats.repository.StatsRepository;
import drlugha.translator.system.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final UserRepository userRepository;

    private final StatsRepository statsRepository;

    public StatsService(UserRepository userRepository, StatsRepository statsRepository) {
        this.userRepository = userRepository;
        this.statsRepository = statsRepository;
    }

    public List<StatsDTO> getStats() {
        return userRepository.generateStats();
    }

    public List<LanguageStatisticsDTO> getLanguageStats() {
        List<LanguageStats> languageStatistics = statsRepository.getLanguageStatistics();

        return languageStatistics.stream()
                .collect(Collectors.groupingBy(LanguageStats::getLanguageId))
                .entrySet().stream()
                .map(entry -> {
                    Long languageId = entry.getKey();
                    List<LanguageStats> statsForLanguage = entry.getValue();

                    LanguageStatisticsDTO dto = new LanguageStatisticsDTO();
                    dto.setLanguageId(languageId);
                    dto.setLanguageName(statsForLanguage.get(0).getLanguageName());

                    List<LanguageStatisticsDTO.BatchTypeStatistics> batchStats =
                            statsForLanguage.stream()
                                    .map(this::getBatchTypeStatistics)
                                    .collect(Collectors.toList());

                    dto.setBatchTypeStatistics(batchStats);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    private LanguageStatisticsDTO.BatchTypeStatistics getBatchTypeStatistics(LanguageStats stats) {
        LanguageStatisticsDTO.BatchTypeStatistics batchTypeStatistics = new LanguageStatisticsDTO.BatchTypeStatistics();
        batchTypeStatistics.setBatchType(stats.getBatchType());
        batchTypeStatistics.setTotalBatchSentencesOrAudios(stats.getTotalBatchSentencesOrAudios());
        batchTypeStatistics.setTotalTranslated(stats.getTotalTranslated());
        batchTypeStatistics.setTotalTextApproved(stats.getTotalTextApproved());
        batchTypeStatistics.setTotalTextRejected(stats.getTotalTextRejected());
        batchTypeStatistics.setTotalTextExpertApproved(stats.getTotalTextExpertApproved());
        batchTypeStatistics.setTotalTextExpertRejected(stats.getTotalTextExpertRejected());
        batchTypeStatistics.setTotalAudioRecorded(stats.getTotalAudioRecorded());
        batchTypeStatistics.setTotalAudioApproved(stats.getTotalAudioApproved());
        batchTypeStatistics.setTotalAudioRejected(stats.getTotalAudioRejected());
        batchTypeStatistics.setTotalAudioExpertApproved(stats.getTotalAudioExpertApproved());
        batchTypeStatistics.setTotalExpertAudioRejected(stats.getTotalExpertAudioRejected());
        batchTypeStatistics.setBatchCount(stats.getBatchCount());
        if (stats.getTotalTranslated() != null) {
            //10% of total Text translated
            batchTypeStatistics.setTextToBeExpertReviewed((int) (0.1 * stats.getTotalTranslated()));
        }

        if (stats.getTotalAudioRecorded() != null) {
            //10% of total Text translated
            batchTypeStatistics.setAudioToBeExpertReviewed((int) (0.1 * stats.getTotalAudioRecorded()));
        }
        return batchTypeStatistics;
    }
}
