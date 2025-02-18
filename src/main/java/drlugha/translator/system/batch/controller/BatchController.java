package drlugha.translator.system.batch.controller;

import drlugha.translator.configs.AmazonClient;
import drlugha.translator.shared.controller.BaseController;
import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.system.batch.dto.*;
import drlugha.translator.system.batch.enums.BatchOrigin;
import drlugha.translator.system.batch.enums.Task;
import drlugha.translator.system.batch.enums.UserBatchRole;
import drlugha.translator.system.batch.model.BatchDetailsEntity;
import drlugha.translator.system.batch.model.BatchEntity;
import drlugha.translator.system.batch.repository.BatchDetailsRepository;
import drlugha.translator.system.batch.repository.BatchRepository;
import drlugha.translator.system.batch.service.BatchService;
import drlugha.translator.system.sentence.dto.CompletedSentencesDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BatchController extends BaseController {

    private final BatchRepository batchRepo;

    private final BatchDetailsRepository batchDetailsRepo;

    private final BatchService batchService;

    private final AmazonClient amazonClient;

    private static final Logger logger = LoggerFactory.getLogger(BatchController.class);

    @GetMapping("/all/batches")
    public ResponseEntity<Page<BatchResponseDTO>> getAllBatches(@RequestParam(required = false) String batchType,
                                                                @RequestParam(name = "page", defaultValue = "0") Integer page,
                                                                @RequestParam(name = "pageSize", defaultValue = "25") Integer pageSize,
                                                                @RequestParam(name = "batchOrigin", required = false) BatchOrigin batchOrigin) {
        return entity(batchService.getAllBatches(batchType, page, pageSize, batchOrigin));
    }

    @PostMapping("/batch")
    public ResponseEntity<ResponseMessage> addBatch(@RequestBody BatchDTO batchDto) {
        return entity(batchService.addBatch(batchDto));
    }

    @PostMapping("/batch/review/{languageId}")
    public ResponseEntity<ResponseMessage> reReviewBatch(@PathVariable Long languageId,
                                                         @RequestBody List<BatchReviewDTO> reviewDTOS) {
        return entity(batchService.addBatchReReview(languageId, reviewDTOS));
    }

    @PutMapping({"/batch"})
    public ResponseEntity<BatchEntity> editBatch(@RequestBody BatchDTO batchDto) {
        return entity(batchService.editBatch(batchDto));
    }

    @DeleteMapping({"/batch"})
    public ResponseEntity<ResponseMessage> deleteBatch(Long batchNo) {
        return entity(batchService.deleteBatch(batchNo));
    }

    @DeleteMapping({"/batch-details"})
    public ResponseEntity<ResponseMessage> deleteBatchDetails(Long batchDetailsId) {
        return entity(batchService.deleteBatchDetails(batchDetailsId));
    }

    @GetMapping({"/all/batch-details"})
    public List<BatchDetailsEntity> getAllBatchDetails() {
        return batchDetailsRepo.findAll();
    }

    @GetMapping({"/batch-details/{batchId}"})
    public List<BatchDetailsDTO> getBatchDetailsByBatch(@PathVariable Long batchId) {
        return batchService.getBatchDetailsByBatch(batchId);
    }

    @PostMapping({"/add/batch-details/{batchNo}"})
    public ResponseEntity<BatchDetailsEntity> addBatchDetails(@RequestBody AddBatchDetailsDTO batchDetailsDto, @PathVariable Long batchNo) {
        return entity(batchService.addBatchDetails(batchDetailsDto, batchNo));
    }

    @PutMapping({"/"})
    public BatchDetailsEntity editBatchDetailsStatus(@RequestBody BatchDetailsEntity batchDetails, @PathVariable Long batchDetailsId) {
        return batchService.editBatchDetailsStatus(batchDetails, batchDetailsId);
    }

    @PutMapping({"/assign/text-verifier/{batchDetailsId}"})
    public BatchDetailsEntity assignTextVerifier(@RequestBody BatchDetailsEntity batchDetails, @PathVariable Long batchDetailsId) {
        return batchService.assignTextVerifier(batchDetails, batchDetailsId);
    }

    @PutMapping({"/assign/second-reviewer/{batchDetailsId}"})
    public BatchDetailsEntity assignExpertReviewer(@RequestBody BatchDetailsEntity batchDetails, @PathVariable Long batchDetailsId) {
        return batchService.assignExpertReviewer(batchDetails, batchDetailsId);
    }

    @PostMapping("/create-prefixed-batch")
    public ResponseEntity<ResponseMessage> createPrefixedBatch(@RequestBody CreatePrefixedBatchDTO batchDto) {
        logger.info("Creating batch with description: {}", batchDto.getDescription());
        return entity(batchService.createPrefixedBatch(batchDto));
    }

    @GetMapping("/move_batch_details")
    public ResponseEntity<ResponseMessage> moveBatchAssignmentsFromLegacyTable() {
        return entity(batchService.moveBatchAssignmentsFromLegacyTable());
    }

    @PutMapping("/assign/{batchDetailsId}")
    public ResponseEntity<?> assignUserRoleToBatch(@PathVariable Long batchDetailsId,
                                                   @RequestParam("role") UserBatchRole role,
                                                   @RequestBody BatchUserAssignmentDTO assignmentDTO) {
        return batchService.assignUserRoleToBatch(batchDetailsId, role, assignmentDTO);
    }

    @PutMapping({"/assign/recorder/{batchDetailsId}"})
    public ResponseEntity assignRecorder(@RequestBody BatchDetailsEntity batchDetails, @PathVariable Long batchDetailsId) {
        return batchService.assignRecorder(batchDetails, batchDetailsId);
    }

    @PutMapping({"/assign/audio-verifier/{batchDetailsId}"})
    public ResponseEntity assignAudioVerifier(@RequestBody BatchDetailsEntity batchDetails, @PathVariable Long batchDetailsId) {
        return batchService.assignAudioVerifier(batchDetails, batchDetailsId);
    }

    @PutMapping({"/batch-status/translated/{batchDetailsId}"})
    public ResponseEntity<ResponseMessage> batchStatusTranslated(@PathVariable Long batchDetailsId) {
        return batchService.markTranslationAsComplete(batchDetailsId);
    }

    @PutMapping({"/batch-status/textVerified/{batchDetailsId}"})
    public ResponseEntity<ResponseMessage> textVerified(@PathVariable Long batchDetailsId) {
        return batchService.markModerationAsComplete(batchDetailsId);
    }

    @GetMapping({"/affected-batch-details"})
    public ResponseEntity getAffectedBatchDetails() {
        return batchService.getAffectedBatcheDetails();
    }

    @PutMapping({"/batch-status/secondVerification/{batchDetailsId}"})
    public ResponseEntity<ResponseMessage> markSecondVerification(@PathVariable Long batchDetailsId) {
        return batchService.markExpertVerificationAsComplete(batchDetailsId);
    }

    @PutMapping({"/batch-status/recorded/{batchDetailsId}"})
    public ResponseEntity<ResponseMessage> batchStatusRecorded(@PathVariable Long batchDetailsId) {
        return batchService.markBatchAsRecorded(batchDetailsId);
    }

    @PutMapping({"/batch-status/audioVerified/{batchDetailsId}"})
    public ResponseEntity<ResponseMessage> batchStatusAudioVerified(@PathVariable Long batchDetailsId,
                                                                    @RequestParam(value = "expertReview", defaultValue = "false") boolean expertReview) {
        return entity(batchService.markAudioReviewAsComplete(batchDetailsId, expertReview));
    }

    @GetMapping({"/user-batch-details"})
    public BatchInfoDTO getBatchDetailsByTask(@RequestParam Long userId, @RequestParam(defaultValue = "TRANSLATION") Task task) {
        return batchService.getBatchDetailsByTask(userId, task);
    }

    @GetMapping({"batch-details/completed-sentences"})
    public ResponseEntity<CompletedSentencesDto> getCompletedSentencesPerBatchDetails(Long batchDetailsId) {
        return entity(batchService.getCompletedSentencesPerBatchDetails(batchDetailsId));
    }

    @GetMapping({"batch-details/stats"})
    public ResponseEntity<List<BatchInfoStatsDTO>> getCompletedSentencesPerBatchDetails() {
        return batchService.getBatchStats();
    }

    @GetMapping("/voice/download")
    public ResponseEntity<byte[]> downloadVoiceFiles(@RequestParam Long batchDetailsId) {
        if (batchDetailsId == null) {
            return ResponseEntity.badRequest().body("Please provide batch details id".getBytes());
        }

        return batchService.getCompletedSentences(batchDetailsId);
    }


    @PostMapping("/updateAudioUrls")
    public ResponseEntity<String> updateAudioUrls(@RequestBody List<Long> batchDetailsIds) {
        try {
            amazonClient.generateAndHandleUploadUrls(batchDetailsIds);
            return ResponseEntity.ok("Upload URLs generated successfully for batchDetailsIds: " + batchDetailsIds);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error generating upload URLs: " + e.getMessage());
        }
    }

    @GetMapping({"batch-details/expert-reviewed"})
    public ResponseEntity getSentencesAfterExpertReview(Long batchDetailsId) {
        return batchService.getTranslatedSentences(batchDetailsId);
    }

    @CrossOrigin(exposedHeaders = "Content-Disposition")
    @GetMapping("batch-details/download")
    public ResponseEntity downloadBatchDetails(@RequestParam List<Long> batchDetailsIds,
                                               @RequestParam("excelOnly") boolean excelOnly) throws Exception {
        return batchService.downloadBatchDetails(batchDetailsIds, excelOnly);
    }

    @GetMapping({"expert-reviewed-sentences"})
    public ResponseEntity getExpertReviewedSentences(Long languageId) {
        return batchService.getExpertReviewedSentences(languageId);
    }

    @GetMapping({"audio-batches/populate"})
    public ResponseEntity<ResponseMessage> populateAudioBatchesFromS3(String name, Long languageId) {
        return amazonClient.populateAudioBatchesFromS3(name, languageId);
    }

    @PostMapping("batch/feedback")
    public ResponseEntity<?> feedbackBatch(@RequestBody FeedbackDTO dto) {
        return ResponseEntity.ok(batchService.createBatchFromFeedback(dto));
    }
}
