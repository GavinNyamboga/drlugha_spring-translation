package drlugha.translator.system.sentence.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drlugha.translator.system.sentence.dto.RejectTranslationDto;
import drlugha.translator.system.sentence.dto.SentenceToReviewDto;
import drlugha.translator.system.sentence.dto.TranslateSentenceDTO;
import drlugha.translator.system.sentence.dto.TranslatedSentencesPerBatchDto;
import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.system.batch.enums.BatchStatus;
import drlugha.translator.shared.enums.StatusTypes;
import drlugha.translator.system.sentence.repository.TranslatedSentenceRepository;
import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.system.batch.service.BatchService;
import drlugha.translator.system.sentence.service.TranslatedSentenceService;
import drlugha.translator.auth.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
public class TranslatedSentenceController {
    @Autowired
    TranslatedSentenceService translatedSvc;

    @Autowired
    TranslatedSentenceRepository translatedRepo;

    @Autowired
    BatchService batchService;

    @Autowired
    ObjectMapper objectMapper;

    @PostMapping({"/translate/sentence/{sentenceId}"})
    public TranslatedSentenceEntity translateSentence(@RequestBody TranslateSentenceDTO translateSentenceDto, @PathVariable Long sentenceId) throws Exception {
        return this.translatedSvc.newtranslateSentence(translateSentenceDto, sentenceId);
    }

    @GetMapping({"/fetch/translatedsentence"})
    public List<TranslatedSentenceEntity> allTranslatedSentences(@RequestParam(defaultValue = "0") int pageNo, int size) {
        return this.translatedSvc.getTranslatedSentencesByPage(pageNo, size);
    }

    @GetMapping({"/all/translatedsentence"})
    public List<TranslatedSentenceEntity> totalTranslatedSentences() {
        return this.translatedRepo.findAll();
    }

    @GetMapping({"/translated-sentences"})
    public List<TranslatedSentencesPerBatchDto> translatedSentencesPerBatchDetailsId(@RequestParam Long batchDetailsId) {
        return this.translatedSvc.getTranslatedSentencesPerBatchDetails(batchDetailsId);
    }

    @GetMapping({"/approved/translatedsentence"})
    public List<TranslatedSentenceEntity> getTranslatedSentenceByStatus() {
        return this.translatedSvc.getTranslatedByStatus();
    }

    @GetMapping({"/reviewer/translatedsentence"})
    public ResponseEntity<SentenceToReviewDto> sentencesToReview(@RequestParam Long userId, @RequestParam(defaultValue = "assignedTextVerifier") BatchStatus batchStatus, @RequestParam(required = false) Long batchDetailsId) {
        return this.batchService.reviewerAssignedTasks(userId, batchStatus, batchDetailsId);
    }

    @GetMapping({"/second-reviewer/translatedsentence"})
    public ResponseEntity<SentenceToReviewDto> sentencesForSecondReview(@RequestParam Long userId, @RequestParam(defaultValue = "assignedExpertReviewer") BatchStatus batchStatus, @RequestParam(required = false) Long batchDetailsId) {
        return this.batchService.expertReviewerAssignedTasks(userId, batchStatus, batchDetailsId);
    }

    @GetMapping({"/users/rejected/translatedsentences"})
    public List<TranslatedSentenceEntity> rejectedTranslatedSentences(@RequestParam(defaultValue = "rejected") StatusTypes reviewStatus, @RequestParam Long userId) {
        return this.translatedRepo.findByReviewStatusAndAssignedTranslator(reviewStatus, userId);
    }

    @GetMapping({"/fetch/translatedsentence/{id}"})
    public Optional<TranslatedSentenceEntity> singleTranslatedSentence(@PathVariable Long id) {
        return this.translatedRepo.findById(id);
    }

    @PutMapping({"/update/translatedsentence/{id}"})
    public ResponseMessage updateTranslatedSentence(@RequestBody TranslatedSentenceEntity translatedSentence, @PathVariable Long id) throws JsonProcessingException {
        try {
            TranslatedSentenceEntity updatedTranslatedSentence = this.translatedSvc.editTranslatedSentence(translatedSentence, id);
            return new ResponseMessage(this.objectMapper.writeValueAsString(updatedTranslatedSentence));
        } catch (NoSuchElementException e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @PutMapping({"/approve/translatedsentence/{id}"})
    public ResponseMessage approveTranslatedStatus(@PathVariable Long id) throws JsonProcessingException {
        try {
            TranslatedSentenceEntity updatedTranslatedSentenceStatus = this.translatedSvc.approveTranslatedSentence(id);
            return new ResponseMessage("Translation Approved");
        } catch (NoSuchElementException e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @PutMapping({"/reject/translatedsentence"})
    public ResponseEntity<ResponseMessage> rejectTranslatedStatus(@RequestBody RejectTranslationDto rejectTranslationDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return this.translatedSvc.rejectTranslatedSentence(rejectTranslationDto, userDetails.getUsername());
    }

    @PutMapping({"/correct/translatedsentence/{id}"})
    public ResponseMessage correctTranslationText(@RequestBody TranslateSentenceDTO translationText, @PathVariable Long id) {
        return this.translatedSvc.correctTranslation(translationText, id);
    }

    @PutMapping({"/translatedsentence/expert-approve/{id}"})
    public ResponseMessage secondApproveTranslatedStatus(@PathVariable Long id) throws JsonProcessingException {
        try {
            this.translatedSvc.expertApproveTranslatedSentence(id);
            return new ResponseMessage("Translation Approved by expert");
        } catch (NoSuchElementException e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @PutMapping({"/translatedsentence/expert-reject"})
    public ResponseEntity<ResponseMessage> expertRejectTranslatedStatus(@RequestBody RejectTranslationDto rejectTranslationDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return this.translatedSvc.expertRejectTranslatedSentence(rejectTranslationDto, userDetails.getUsername());
    }

    @DeleteMapping({"/delete/translatedsentence/{id}"})
    public ResponseMessage deleteTranslatedSentence(@PathVariable Long id) {
        try {
            this.translatedRepo.deleteById(id);
            return new ResponseMessage("Deleted successfully");
        } catch (EmptyResultDataAccessException e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @DeleteMapping({"/delete-duplicate-translations"})
    public ResponseEntity<ResponseMessage> deleteDuplicateTranslations() {
        return this.translatedSvc.deleteDuplicateTranslations();
    }
}
