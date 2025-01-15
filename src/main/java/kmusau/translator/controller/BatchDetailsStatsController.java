package kmusau.translator.controller;

import java.util.List;
import kmusau.translator.DTOs.stats.TotalUserStatsDto;
import kmusau.translator.DTOs.stats.TotalsDto;
import kmusau.translator.response.ResponseMessage;
import kmusau.translator.service.BatchDetailsStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class BatchDetailsStatsController {
    private final BatchDetailsStatsService batchDetailsStatsService;

    public BatchDetailsStatsController(BatchDetailsStatsService batchDetailsStatsService) {
        this.batchDetailsStatsService = batchDetailsStatsService;
    }

    @GetMapping({"/stats/batch-details"})
    public ResponseEntity getBatchDetailsStatsById(Long batchDetailsId) {
        return this.batchDetailsStatsService.getBatchDetailsStatsById(batchDetailsId);
    }

    @GetMapping({"/stats/all-batch-details"})
    public ResponseEntity getBatchDetailsStats(String batchType) {
        return this.batchDetailsStatsService.getBatchDetailsStats(batchType);
    }

    @GetMapping({"/stats/user/batch-details"})
    public ResponseEntity getUsersStatsForEachBatchDetails(Long userId) {
        return this.batchDetailsStatsService.findUsersStatsForEachBatchDetails(userId);
    }

    @GetMapping({"/stats/user"})
    public ResponseEntity getTotalUserStats(Long userId) {
        return this.batchDetailsStatsService.findTotalUserStats(userId);
    }

    @GetMapping({"/stats/users"})
    public ResponseEntity getTotalUsersStats(String batchType) {
        return this.batchDetailsStatsService.findAllUsersStats(batchType);
    }

    @PostMapping({"/populate-stats"})
    public ResponseEntity<ResponseMessage> populateStatsForExistingBatches() {
        return this.batchDetailsStatsService.populateStatsForExistingBatches();
    }

    @GetMapping({"/stats/totals"})
    public ResponseEntity<TotalsDto> getTotalSentencesAndTranslatedSentences() {
        return this.batchDetailsStatsService.getTotalSentencesAndTranslatedSentences();
    }

    @GetMapping({"/stats/users/range"})
    public ResponseEntity<List<TotalUserStatsDto>> getUsersStatsByDateRange(String batchType, String startDate, String endDate) {
        return this.batchDetailsStatsService.getTotalUserStats(batchType, startDate, endDate);
    }
}
