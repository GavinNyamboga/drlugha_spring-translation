package drlugha.translator.system.sentence.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drlugha.translator.auth.service.UserDetailsImpl;
import drlugha.translator.shared.controller.BaseController;
import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.shared.enums.StatusTypes;
import drlugha.translator.system.batch.enums.BatchStatus;
import drlugha.translator.system.batch.service.BatchService;
import drlugha.translator.system.sentence.dto.RejectTranslationDto;
import drlugha.translator.system.sentence.dto.SentenceToReviewDto;
import drlugha.translator.system.sentence.dto.TranslateSentenceDTO;
import drlugha.translator.system.sentence.dto.TranslatedSentencesPerBatchDto;
import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.system.sentence.repository.TranslatedSentenceRepository;
import drlugha.translator.system.sentence.service.TranslatedSentenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class TranslatedSentenceController extends BaseController {
    private final TranslatedSentenceService translatedSvc;

    private final TranslatedSentenceRepository translatedRepo;

    private final BatchService batchService;

    private final ObjectMapper objectMapper;

    @PostMapping({"/translate/sentence/{sentenceId}"})
    public ResponseEntity<TranslatedSentenceEntity> translateSentence(@RequestBody TranslateSentenceDTO translateSentenceDto, @PathVariable Long sentenceId) throws Exception {
        return entity(translatedSvc.newtranslateSentence(translateSentenceDto, sentenceId));
    }

    @GetMapping({"/fetch/translatedsentence"})
    public List<TranslatedSentenceEntity> allTranslatedSentences(@RequestParam(defaultValue = "0") int pageNo, int size) {
        return translatedSvc.getTranslatedSentencesByPage(pageNo, size);
    }

    @GetMapping({"/all/translatedsentence"})
    public List<TranslatedSentenceEntity> totalTranslatedSentences() {
        return translatedRepo.findAll();
    }

    @GetMapping({"/translated-sentences"})
    public List<TranslatedSentencesPerBatchDto> translatedSentencesPerBatchDetailsId(@RequestParam Long batchDetailsId) {
        return translatedSvc.getTranslatedSentencesPerBatchDetails(batchDetailsId);
    }

    @GetMapping({"/approved/translatedsentence"})
    public List<TranslatedSentenceEntity> getTranslatedSentenceByStatus() {
        return translatedSvc.getTranslatedByStatus();
    }

    @GetMapping({"/reviewer/translatedsentence"})
    public ResponseEntity<SentenceToReviewDto> sentencesToReview(@RequestParam Long userId,
                                                                 @RequestParam(defaultValue = "ASSIGNED_TEXT_VERIFIER") BatchStatus batchStatus,
                                                                 @RequestParam(required = false) Long batchDetailsId) {
        return batchService.reviewerAssignedTasks(userId, batchStatus, batchDetailsId);
    }

    @GetMapping({"/second-reviewer/translatedsentence"})
    public ResponseEntity<SentenceToReviewDto> sentencesForSecondReview(@RequestParam Long userId,
                                                                        @RequestParam(defaultValue = "ASSIGNED_EXPERT_REVIEWER") BatchStatus batchStatus,
                                                                        @RequestParam(required = false) Long batchDetailsId) {
        return batchService.expertReviewerAssignedTasks(userId, batchStatus, batchDetailsId);
    }

    @GetMapping({"/users/rejected/translatedsentences"})
    public List<TranslatedSentenceEntity> rejectedTranslatedSentences(@RequestParam(defaultValue = "REJECTED") StatusTypes reviewStatus,
                                                                      @RequestParam Long userId) {
        return translatedRepo.findByReviewStatusAndAssignedTranslator(reviewStatus, userId);
    }

    @GetMapping({"/fetch/translatedsentence/{id}"})
    public Optional<TranslatedSentenceEntity> singleTranslatedSentence(@PathVariable Long id) {
        return translatedRepo.findById(id);
    }

    @PutMapping({"/update/translatedsentence/{id}"})
    public ResponseMessage updateTranslatedSentence(@RequestBody TranslatedSentenceEntity translatedSentence,
                                                    @PathVariable Long id) throws JsonProcessingException {
        try {
            TranslatedSentenceEntity updatedTranslatedSentence = translatedSvc.editTranslatedSentence(translatedSentence, id);
            return new ResponseMessage(objectMapper.writeValueAsString(updatedTranslatedSentence));
        } catch (NoSuchElementException e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @PutMapping({"/approve/translatedsentence/{id}"})
    public ResponseMessage approveTranslatedStatus(@PathVariable Long id) throws JsonProcessingException {
        try {
            translatedSvc.approveTranslatedSentence(id);
            return new ResponseMessage("Translation Approved");
        } catch (NoSuchElementException e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @PutMapping({"/reject/translatedsentence"})
    public ResponseEntity<ResponseMessage> rejectTranslatedStatus(@RequestBody RejectTranslationDto rejectTranslationDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return translatedSvc.rejectTranslatedSentence(rejectTranslationDto, userDetails.getUsername());
    }

    @PutMapping({"/correct/translatedsentence/{id}"})
    public ResponseMessage correctTranslationText(@RequestBody TranslateSentenceDTO translationText, @PathVariable Long id) {
        return translatedSvc.correctTranslation(translationText, id);
    }

    @PutMapping({"/translatedsentence/expert-approve/{id}"})
    public ResponseMessage secondApproveTranslatedStatus(@PathVariable Long id) throws JsonProcessingException {
        try {
            translatedSvc.expertApproveTranslatedSentence(id);
            return new ResponseMessage("Translation Approved by expert");
        } catch (NoSuchElementException e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @PutMapping({"/translatedsentence/expert-reject"})
    public ResponseEntity<ResponseMessage> expertRejectTranslatedStatus(@RequestBody RejectTranslationDto rejectTranslationDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return translatedSvc.expertRejectTranslatedSentence(rejectTranslationDto, userDetails.getUsername());
    }

    @DeleteMapping({"/delete/translatedsentence/{id}"})
    public ResponseMessage deleteTranslatedSentence(@PathVariable Long id) {
        try {
            translatedRepo.deleteById(id);
            return new ResponseMessage("Deleted successfully");
        } catch (EmptyResultDataAccessException e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @DeleteMapping({"/delete-duplicate-translations"})
    public ResponseEntity<ResponseMessage> deleteDuplicateTranslations() {
        return translatedSvc.deleteDuplicateTranslations();
    }
}
