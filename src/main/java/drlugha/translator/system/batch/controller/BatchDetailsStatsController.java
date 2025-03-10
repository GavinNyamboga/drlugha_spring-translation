package drlugha.translator.system.batch.controller;

import drlugha.translator.shared.controller.BaseController;
import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.system.batch.enums.BatchStatus;
import drlugha.translator.system.batch.model.BatchDetailsStatsEntity;
import drlugha.translator.system.batch.service.BatchDetailsStatsService;
import drlugha.translator.system.stats.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BatchDetailsStatsController extends BaseController {
    private final BatchDetailsStatsService batchDetailsStatsService;

    public BatchDetailsStatsController(BatchDetailsStatsService batchDetailsStatsService) {
        this.batchDetailsStatsService = batchDetailsStatsService;
    }

    @GetMapping({"/stats/batch-details"})
    public ResponseEntity<BatchDetailsStatsEntity> getBatchDetailsStatsById(Long batchDetailsId) {
        return entity(batchDetailsStatsService.getBatchDetailsStatsById(batchDetailsId));
    }

    @GetMapping({"/stats/all-batch-details"})
    public ResponseEntity<Page<BatchDetailsStats>> getBatchDetailsStats(String batchType,
                                                                        @RequestParam(name = "page", defaultValue = "0") Integer page,
                                                                        @RequestParam(name = "pageSize", defaultValue = "25") Integer pageSize,
                                                                        @RequestParam(name = "languageId", required = false) Long languageId,
                                                                        @RequestParam(name = "status", required = false) BatchStatus status,
                                                                        @RequestParam(name = "source", required = false) String source) {
        return entity(batchDetailsStatsService.getBatchDetailsStats(batchType, page, pageSize, languageId, status, source));
    }

    @GetMapping({"/stats/user/batch-details"})
    public ResponseEntity<RoleStatsDTO> getUsersStatsForEachBatchDetails(Long userId) {
        return entity(batchDetailsStatsService.findUsersStatsForEachBatchDetails(userId));
    }

    @GetMapping({"/stats/user"})
    public ResponseEntity<UserStatsDTO> getTotalUserStats(Long userId) {
        return entity(batchDetailsStatsService.findTotalUserStats(userId));
    }

    @GetMapping({"/stats/users"})
    public ResponseEntity<List<UserStatsDTO>> getTotalUsersStats(String batchType) {
        return entity(batchDetailsStatsService.findAllUsersStats(batchType));
    }

    @PostMapping({"/populate-stats"})
    public ResponseEntity<ResponseMessage> populateStatsForExistingBatches() {
        return entity(batchDetailsStatsService.populateStatsForExistingBatches());
    }

    @GetMapping({"/stats/totals"})
    public ResponseEntity<TotalsDTO> getTotalSentencesAndTranslatedSentences() {
        return entity(batchDetailsStatsService.getTotalSentencesAndTranslatedSentences());
    }

    @GetMapping({"/stats/users/range"})
    public ResponseEntity<List<TotalUserStatsDto>> getUsersStatsByDateRange(String batchType, String startDate, String endDate) {
        return entity(batchDetailsStatsService.getTotalUserStats(batchType, startDate, endDate));
    }
}
