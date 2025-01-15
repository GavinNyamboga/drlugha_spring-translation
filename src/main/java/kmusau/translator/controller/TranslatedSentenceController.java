package kmusau.translator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import kmusau.translator.DTOs.translatedSentencesDTOs.RejectTranslationDto;
import kmusau.translator.DTOs.translatedSentencesDTOs.SentenceToReviewDto;
import kmusau.translator.DTOs.translatedSentencesDTOs.TranslateSentenceDto;
import kmusau.translator.DTOs.translatedSentencesDTOs.TranslatedSentencesPerBatchDto;
import kmusau.translator.entity.TranslatedSentenceEntity;
import kmusau.translator.enums.BatchStatus;
import kmusau.translator.enums.StatusTypes;
import kmusau.translator.repository.TranslatedSentenceRepository;
import kmusau.translator.response.ResponseMessage;
import kmusau.translator.service.SentenceBatchService;
import kmusau.translator.service.TranslatedSentenceService;
import kmusau.translator.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TranslatedSentenceController {
    @Autowired
    TranslatedSentenceService translatedSvc;

    @Autowired
    TranslatedSentenceRepository translatedRepo;

    @Autowired
    SentenceBatchService sentenceBatchService;

    @Autowired
    ObjectMapper objectMapper;

    @PostMapping({"/translate/sentence/{sentenceId}"})
    public TranslatedSentenceEntity translateSentence(@RequestBody TranslateSentenceDto translateSentenceDto, @PathVariable Long sentenceId) throws Exception {
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
        return this.sentenceBatchService.reviewerAssignedTasks(userId, batchStatus, batchDetailsId);
    }

    @GetMapping({"/second-reviewer/translatedsentence"})
    public ResponseEntity<SentenceToReviewDto> sentencesForSecondReview(@RequestParam Long userId, @RequestParam(defaultValue = "assignedExpertReviewer") BatchStatus batchStatus, @RequestParam(required = false) Long batchDetailsId) {
        return this.sentenceBatchService.expertReviewerAssignedTasks(userId, batchStatus, batchDetailsId);
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
    public ResponseMessage correctTranslationText(@RequestBody TranslateSentenceDto translationText, @PathVariable Long id) {
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
