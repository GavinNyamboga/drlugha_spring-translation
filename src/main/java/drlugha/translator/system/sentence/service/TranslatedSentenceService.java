package drlugha.translator.system.sentence.service;

import drlugha.translator.auth.service.AuthenticationService;
import drlugha.translator.configs.AmazonClient;
import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.shared.enums.StatusTypes;
import drlugha.translator.shared.exception.BadRequestException;
import drlugha.translator.system.batch.enums.BatchStatus;
import drlugha.translator.system.batch.enums.BatchType;
import drlugha.translator.system.batch.enums.UserBatchRole;
import drlugha.translator.system.batch.model.BatchDetailsEntity;
import drlugha.translator.system.batch.model.BatchDetailsUserAssignment;
import drlugha.translator.system.batch.repository.BatchDetailsRepository;
import drlugha.translator.system.batch.repository.BatchDetailsStatsRepository;
import drlugha.translator.system.batch.repository.BatchDetailsUserAssigmentRepo;
import drlugha.translator.system.sentence.dto.RejectTranslationDto;
import drlugha.translator.system.sentence.dto.TranslateSentenceDTO;
import drlugha.translator.system.sentence.dto.TranslatedSentencesPerBatchDto;
import drlugha.translator.system.sentence.model.ExpertCommentEntity;
import drlugha.translator.system.sentence.model.ModeratorCommentEntity;
import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.system.sentence.model.TranslatedSentenceLogsEntity;
import drlugha.translator.system.sentence.repository.ExpertCommentRepo;
import drlugha.translator.system.sentence.repository.ModeratorCommentRepo;
import drlugha.translator.system.sentence.repository.TranslatedSentenceLogsRepo;
import drlugha.translator.system.sentence.repository.TranslatedSentenceRepository;
import drlugha.translator.system.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@RequiredArgsConstructor
public class TranslatedSentenceService {

    private final TranslatedSentenceRepository translatedRepo;

    private final BatchDetailsRepository batchDetailsRepo;

    private final ModeratorCommentRepo moderatorCommentRepo;

    private final ExpertCommentRepo expertCommentRepo;

    private final BatchDetailsStatsRepository batchDetailsStatsRepository;

    private final TranslatedSentenceLogsRepo translatedSentenceLogsRepo;

    private final AuthenticationService authenticationService;

    private final AmazonClient amazonClient;

    private final BatchDetailsUserAssigmentRepo batchDetailsUserAssigmentRepo;

    public List<TranslatedSentenceEntity> getTranslatedSentencesByPage(int pageNo, int size) {
        Pageable paging = PageRequest.of(pageNo, size);
        Page<TranslatedSentenceEntity> pagedResult = translatedRepo.findAll(paging);

        if (pagedResult.hasContent()) {
            return pagedResult.getContent();
        } else
            return new ArrayList<>();
    }

    public List<TranslatedSentenceEntity> getTranslatedByStatus() {
        return translatedRepo.findByReviewStatusAndRecordedStatus();
    }


    @Transactional
    public TranslatedSentenceEntity newtranslateSentence(TranslateSentenceDTO translateSentenceDto, Long sentenceId) {
        if (translateSentenceDto.getTranslatedText() == null || translateSentenceDto.getTranslatedText().isEmpty()) {
            throw new BadRequestException("Translated text not captured");
        }
        User currentUser = authenticationService.getCurrentUser();

        BatchDetailsEntity batchDetailsEntity = batchDetailsRepo.findById(translateSentenceDto.getBatchDetailsId()).orElse(null);
        if (batchDetailsEntity == null)
            throw new BadRequestException("Batch details not found");

        BatchType batchType = batchDetailsEntity.getBatch().getBatchType();

        List<TranslatedSentenceEntity> existingTranslations = translatedRepo.findAllBySentenceIdAndBatchDetailsId(sentenceId, translateSentenceDto.getBatchDetailsId());
        TranslatedSentenceEntity translatedSentence;
        if (!existingTranslations.isEmpty())
            translatedSentence = existingTranslations.get(0);
        else
            translatedSentence = new TranslatedSentenceEntity();
        translatedSentence.setTranslatedText(translateSentenceDto.getTranslatedText());

        BatchDetailsEntity batchDetails = batchDetailsRepo.findById(translateSentenceDto.getBatchDetailsId()).orElse(null);
        if (batchDetails == null)
            throw new BadRequestException("Batch details not found");

        translatedSentence.setLanguage(batchDetails.getLanguage());
        translatedSentence.setBatchDetailsId(translateSentenceDto.getBatchDetailsId());
        translatedSentence.setReviewStatus(StatusTypes.UNREVIEWED);
        translatedSentence.setSentenceId(sentenceId);
        translatedSentence.setRecordedBy(currentUser);
        if (translatedSentence.getTranslatedSentenceId() == null) { //Update user stats
            Long userId = currentUser.getUserId();

            UserBatchRole role = UserBatchRole.TEXT_TRANSLATOR;
            if (batchType == BatchType.AUDIO)
                role = UserBatchRole.AUDIO_TRANSCRIBER;

            List<BatchDetailsUserAssignment> userAssignmentList =
                    batchDetailsUserAssigmentRepo.findByUserIdAndBatchRoleAndBatchDetails_BatchDetailsId(userId, role, batchDetails.getBatchDetailsId());
            if (!userAssignmentList.isEmpty()) {
                for (BatchDetailsUserAssignment userAssignment : userAssignmentList) {
                    int translatedCount = userAssignment.getTranslated() != null ? userAssignment.getTranslated() : 0;
                    userAssignment.setTranslated(translatedCount + 1);

                    batchDetailsUserAssigmentRepo.save(userAssignment);
                }
            }
        }

        return translatedRepo.save(translatedSentence);

    }

    public TranslatedSentenceEntity editTranslatedSentence(TranslatedSentenceEntity translatedSentence, Long id) {
        TranslatedSentenceEntity translatedSentence1 = translatedRepo.findById(id).orElse(null);
        if (translatedSentence1 == null)
            throw new BadRequestException("Translated sentence not found");

        if (Objects.nonNull(translatedSentence.getTranslatedText())) {
            translatedSentence1.setTranslatedText(translatedSentence.getTranslatedText());
        }

        translatedSentence1.setReviewStatus(StatusTypes.UNREVIEWED);
        return translatedRepo.save(translatedSentence1);
    }

    @Transactional
    public TranslatedSentenceEntity approveTranslatedSentence(Long id) {
        TranslatedSentenceEntity translatedSentence = translatedRepo.findById(id).orElse(null);
        if (translatedSentence == null)
            throw new BadRequestException("Translated sentence not found");

        translatedSentence.setReviewStatus(StatusTypes.APPROVED);
        if (translatedSentence.getSecondReview() == StatusTypes.REJECTED)
            translatedSentence.setSecondReview(StatusTypes.UNREVIEWED);
        TranslatedSentenceEntity updatedSentence = translatedRepo.save(translatedSentence);

        TranslatedSentenceLogsEntity translatedSentenceLogs = getTranslatedSentenceLogsEntity(updatedSentence);
        translatedSentenceLogs.setDateModerated(new Date());
        translatedSentenceLogsRepo.save(translatedSentenceLogs);

        Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(updatedSentence.getBatchDetailsId());
        if (optionalBatchDetails.isPresent()) {
            BatchDetailsEntity batchDetails = optionalBatchDetails.get();
            if (batchDetails.getBatchStatus() == BatchStatus.ASSIGNED_TEXT_VERIFIER) { //Update user stats
                Long userId = translatedSentence.getRecordedBy().getUserId();

                UserBatchRole role = UserBatchRole.TEXT_TRANSLATOR;
                if (batchDetails.getBatch().getBatchType() == BatchType.AUDIO)
                    role = UserBatchRole.AUDIO_TRANSCRIBER;

                List<BatchDetailsUserAssignment> userAssignmentList =
                        batchDetailsUserAssigmentRepo.findByUserIdAndBatchRoleAndBatchDetails_BatchDetailsId(userId, role, batchDetails.getBatchDetailsId());
                if (!userAssignmentList.isEmpty()) {
                    for (BatchDetailsUserAssignment userAssignment : userAssignmentList) {
                        int approved = userAssignment.getTextApproved() != null ? userAssignment.getTextApproved() : 0;
                        userAssignment.setTextApproved(approved + 1);

                        batchDetailsUserAssigmentRepo.save(userAssignment);
                    }
                }
            }
        }

        return updatedSentence;
    }

    @Transactional
    public ResponseEntity<ResponseMessage> rejectTranslatedSentence(RejectTranslationDto rejectTranslationDto, String username) {
        ResponseEntity<ResponseMessage> body = validateDto(rejectTranslationDto);
        if (body != null) return body;

        Optional<TranslatedSentenceEntity> optionalTranslatedSentence = translatedRepo
                .findById(rejectTranslationDto.getTranslatedSentenceId());

        if (optionalTranslatedSentence.isEmpty())
            return ResponseEntity.badRequest().body(new ResponseMessage("Error! The translated sentence you are trying to reject does not exist"));

        TranslatedSentenceEntity translatedSentence = optionalTranslatedSentence.get();
        if (!translatedSentence.getBatchDetails().getTranslationVerifiedBy().getUsername().matches(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMessage("You are unauthorized to reject this translation"));
        }
        translatedSentence.setReviewStatus(StatusTypes.REJECTED);
        TranslatedSentenceEntity updatedSentence = translatedRepo.save(translatedSentence);

        TranslatedSentenceLogsEntity translatedSentenceLogs = getTranslatedSentenceLogsEntity(updatedSentence);
        translatedSentenceLogs.setDateModerated(new Date());
        translatedSentenceLogsRepo.save(translatedSentenceLogs);

        ModeratorCommentEntity moderatorCommentEntity =
                moderatorCommentRepo.findAllByTranslatedSentence_TranslatedSentenceId(translatedSentence.getTranslatedSentenceId());

        if (moderatorCommentEntity == null)
            moderatorCommentEntity = new ModeratorCommentEntity();
        moderatorCommentEntity.setTranslatedSentence(translatedSentence);
        moderatorCommentEntity.setComment(rejectTranslationDto.getComment());
        moderatorCommentRepo.save(moderatorCommentEntity);

        Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(translatedSentence.getBatchDetailsId());
        if (optionalBatchDetails.isPresent()) {
            BatchDetailsEntity batchDetails = optionalBatchDetails.get();
            if (batchDetails.getBatchStatus() == BatchStatus.ASSIGNED_TEXT_VERIFIER) {
                Long userId = translatedSentence.getRecordedBy().getUserId();

                UserBatchRole role = UserBatchRole.TEXT_TRANSLATOR;
                if (batchDetails.getBatch().getBatchType() == BatchType.AUDIO)
                    role = UserBatchRole.AUDIO_TRANSCRIBER;

                List<BatchDetailsUserAssignment> userAssignmentList =
                        batchDetailsUserAssigmentRepo.findByUserIdAndBatchRoleAndBatchDetails_BatchDetailsId(userId, role, batchDetails.getBatchDetailsId());
                if (!userAssignmentList.isEmpty()) {
                    for (BatchDetailsUserAssignment userAssignment : userAssignmentList) {
                        int rejected = userAssignment.getTextRejected() != null ? userAssignment.getTextRejected() : 0;
                        userAssignment.setTextRejected(rejected + 1);

                        batchDetailsUserAssigmentRepo.save(userAssignment);
                    }
                }
            }
        }

        return ResponseEntity.ok(new ResponseMessage("Translation successfully rejected"));
    }

    public ResponseMessage correctTranslation(TranslateSentenceDTO translatedSentenceText, Long translatedSentenceId) {

        TranslatedSentenceEntity translatedSentenceEntity = translatedRepo.findById(translatedSentenceId).get();
        if (Objects.nonNull(translatedSentenceText.getTranslatedText())) {
            translatedSentenceEntity.setTranslatedText(translatedSentenceText.getTranslatedText());
        }
        translatedRepo.save(translatedSentenceEntity);

        return new ResponseMessage("Translation correction has been submitted");
    }

    public TranslatedSentenceEntity expertApproveTranslatedSentence(Long id) {

        TranslatedSentenceEntity translatedSentence = translatedRepo.findById(id).orElse(null);
        if (translatedSentence == null)
            throw new BadRequestException("The translated sentence does not exist");

        translatedSentence.setSecondReview(StatusTypes.APPROVED);
        TranslatedSentenceEntity updatedSentence = translatedRepo.save(translatedSentence);

        TranslatedSentenceLogsEntity translatedSentenceLogs = getTranslatedSentenceLogsEntity(updatedSentence);
        translatedSentenceLogs.setDateExpertModerated(new Date());
        translatedSentenceLogsRepo.save(translatedSentenceLogs);

        Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(translatedSentence.getBatchDetailsId());

        if (optionalBatchDetails.isPresent()) {
            BatchDetailsEntity batchDetails = optionalBatchDetails.get();
            Long userId = batchDetails.getSecondReviewerId();
            User currentUser = authenticationService.getCurrentUser();
            if (currentUser != null)
                userId = currentUser.getUserId();

            UserBatchRole role = UserBatchRole.TEXT_TRANSLATOR;
            if (batchDetails.getBatch().getBatchType() == BatchType.AUDIO)
                role = UserBatchRole.AUDIO_TRANSCRIBER;

            List<BatchDetailsUserAssignment> userAssignmentList =
                    batchDetailsUserAssigmentRepo.findByUserIdAndBatchRoleAndBatchDetails_BatchDetailsId(userId, role, batchDetails.getBatchDetailsId());
            if (!userAssignmentList.isEmpty()) {
                for (BatchDetailsUserAssignment userAssignment : userAssignmentList) {
                    int approved = userAssignment.getTextExpertApproved() != null ? userAssignment.getTextExpertApproved() : 0;
                    userAssignment.setTextExpertApproved(approved + 1);

                    batchDetailsUserAssigmentRepo.save(userAssignment);
                }
            }

        }

        return updatedSentence;
    }

    public ResponseEntity<ResponseMessage> expertRejectTranslatedSentence(RejectTranslationDto rejectTranslationDto, String username) {

        ResponseEntity<ResponseMessage> validationResponse = validateDto(rejectTranslationDto);
        if (validationResponse != null)
            return validationResponse;

        Optional<TranslatedSentenceEntity> optionalTranslatedSentence = translatedRepo
                .findById(rejectTranslationDto.getTranslatedSentenceId());

        if (optionalTranslatedSentence.isEmpty())
            return ResponseEntity.badRequest().body(new ResponseMessage("Error! The translated sentence you are trying to reject does not exist"));

        TranslatedSentenceEntity translatedStnc = optionalTranslatedSentence.get();

        if (!translatedStnc.getBatchDetails().getSecondReviewer().getUsername().matches(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMessage("You are unauthorized to reject this translation"));
        }

        translatedStnc.setSecondReview(StatusTypes.REJECTED);

        TranslatedSentenceEntity updatedSentence = translatedRepo.save(translatedStnc);
        TranslatedSentenceLogsEntity translatedSentenceLogs = getTranslatedSentenceLogsEntity(updatedSentence);
        translatedSentenceLogs.setDateExpertModerated(new Date());
        translatedSentenceLogsRepo.save(translatedSentenceLogs);

        ExpertCommentEntity expertCommentEntity =
                expertCommentRepo.findAllByTranslatedSentence_TranslatedSentenceId(translatedStnc.getTranslatedSentenceId());

        if (expertCommentEntity == null)
            expertCommentEntity = new ExpertCommentEntity();
        expertCommentEntity.setTranslatedSentence(translatedStnc);
        expertCommentEntity.setComment(rejectTranslationDto.getComment());
        expertCommentRepo.save(expertCommentEntity);

        Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(translatedStnc.getBatchDetailsId());
        if (optionalBatchDetails.isPresent()) {
            BatchDetailsEntity batchDetails = optionalBatchDetails.get();
            Long userId = batchDetails.getSecondReviewerId();
            User currentUser = authenticationService.getCurrentUser();
            if (currentUser != null)
                userId = currentUser.getUserId();

            UserBatchRole role = UserBatchRole.TEXT_TRANSLATOR;
            if (batchDetails.getBatch().getBatchType() == BatchType.AUDIO)
                role = UserBatchRole.AUDIO_TRANSCRIBER;

            List<BatchDetailsUserAssignment> userAssignmentList =
                    batchDetailsUserAssigmentRepo.findByUserIdAndBatchRoleAndBatchDetails_BatchDetailsId(userId, role, batchDetails.getBatchDetailsId());
            if (!userAssignmentList.isEmpty()) {
                for (BatchDetailsUserAssignment userAssignment : userAssignmentList) {
                    int rejected = userAssignment.getTextExpertRejected() != null ? userAssignment.getTextExpertRejected() : 0;
                    userAssignment.setTextExpertRejected(rejected + 1);

                    batchDetailsUserAssigmentRepo.save(userAssignment);
                }
            }

        }

        return ResponseEntity.ok(new ResponseMessage("Translation successfully rejected "));
    }

    private static ResponseEntity<ResponseMessage> validateDto(RejectTranslationDto rejectTranslationDto) {
        if (rejectTranslationDto == null)
            return ResponseEntity.badRequest().body(new ResponseMessage("Please provide translated sentence id and comments"));
        if (rejectTranslationDto.getTranslatedSentenceId() == null)
            return ResponseEntity.badRequest().body(new ResponseMessage("Please provide translated sentence id"));
        if (rejectTranslationDto.getComment() == null || rejectTranslationDto.getComment().isBlank())
            return ResponseEntity.badRequest().body(new ResponseMessage("Please provide comments"));
        return null;
    }

    public List<TranslatedSentencesPerBatchDto> getTranslatedSentencesPerBatchDetails(Long batchDetailsId) {
        List<TranslatedSentenceEntity> translatedSentences =
                translatedRepo.findByBatchDetailsId(batchDetailsId, Sort.by(Sort.Direction.DESC, "reviewStatus"));
        List<TranslatedSentencesPerBatchDto> translatedSentencesPerBatchDtos = new ArrayList<>();

        for (TranslatedSentenceEntity translatedSentence : translatedSentences) {
            ModeratorCommentEntity moderatorCommentEntity = moderatorCommentRepo.findAllByTranslatedSentence_TranslatedSentenceId(translatedSentence.getTranslatedSentenceId());
            ExpertCommentEntity expertCommentEntity = expertCommentRepo.findAllByTranslatedSentence_TranslatedSentenceId(translatedSentence.getTranslatedSentenceId());

            String moderatorComment = "";
            String expertComment = "";

            if (translatedSentence.getReviewStatus() == StatusTypes.REJECTED && moderatorCommentEntity != null)
                moderatorComment = moderatorCommentEntity.getComment();
            if (translatedSentence.getSecondReview() == StatusTypes.REJECTED && expertCommentEntity != null)
                expertComment = expertCommentEntity.getComment();

            translatedSentence.getSentence().setAudioLink(amazonClient.generatePresignedUrl(translatedSentence.getSentence().getAudioLink()));
            TranslatedSentencesPerBatchDto sentencesPerBatchDto = new TranslatedSentencesPerBatchDto().toDto(
                    translatedSentence,
                    moderatorComment,
                    expertComment
            );
            translatedSentencesPerBatchDtos.add(sentencesPerBatchDto);
        }
        return translatedSentencesPerBatchDtos;
    }

    @Transactional
    public ResponseEntity<ResponseMessage> deleteDuplicateTranslations() {
        translatedRepo.deleteDuplicateTranslations();
        return ResponseEntity.ok(new ResponseMessage("Duplicates successfully deleted"));
    }

    private TranslatedSentenceLogsEntity getTranslatedSentenceLogsEntity(TranslatedSentenceEntity updatedSentence) {
        TranslatedSentenceLogsEntity translatedSentenceLogs = translatedSentenceLogsRepo.findByTranslatedSentence(updatedSentence);
        if (translatedSentenceLogs == null) {
            translatedSentenceLogs = new TranslatedSentenceLogsEntity();
            translatedSentenceLogs.setTranslatedSentence(updatedSentence);
        }
        return translatedSentenceLogs;
    }
}