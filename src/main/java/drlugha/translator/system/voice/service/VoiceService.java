package drlugha.translator.system.voice.service;

import drlugha.translator.auth.service.AuthenticationService;
import drlugha.translator.configs.AmazonClient;
import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.shared.enums.StatusTypes;
import drlugha.translator.shared.enums.YesNo;
import drlugha.translator.shared.exception.BadRequestException;
import drlugha.translator.shared.exception.NotFoundException;
import drlugha.translator.system.batch.enums.BatchStatus;
import drlugha.translator.system.batch.enums.UserBatchRole;
import drlugha.translator.system.batch.model.BatchDetailsEntity;
import drlugha.translator.system.batch.model.BatchDetailsUserAssignment;
import drlugha.translator.system.batch.repository.BatchDetailsRepository;
import drlugha.translator.system.batch.repository.BatchDetailsStatsRepository;
import drlugha.translator.system.batch.repository.BatchDetailsUserAssigmentRepo;
import drlugha.translator.system.sentence.dto.SentenceToRecordDto;
import drlugha.translator.system.sentence.dto.TranslatedSentenceItemDto;
import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.system.sentence.model.TranslatedSentenceLogsEntity;
import drlugha.translator.system.sentence.repository.TranslatedSentenceLogsRepo;
import drlugha.translator.system.sentence.repository.TranslatedSentenceRepository;
import drlugha.translator.system.user.model.User;
import drlugha.translator.system.voice.model.VoiceEntity;
import drlugha.translator.system.voice.repository.VoiceRepository;
import drlugha.translator.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoiceService {

    private final AuthenticationService authenticationService;

    private final VoiceRepository voiceRepo;

    private final TranslatedSentenceRepository translatedSentenceRepo;

    private final BatchDetailsRepository batchDetailsRepo;

    private final TranslatedSentenceLogsRepo translatedSentenceLogsRepo;

    private final BatchDetailsUserAssigmentRepo batchDetailsUserAssigmentRepo;

    private final JwtUtil jwtUtil;

    private final AmazonClient amazonClient;

    Logger logger = LoggerFactory.getLogger(VoiceService.class);

    public List<VoiceEntity> getVoiceByPage(int pageNo, int size) {
        Pageable paging = PageRequest.of(pageNo, size);
        Page<VoiceEntity> pagedResult = voiceRepo.findAll(paging);

        if (pagedResult.hasContent()) {
            return pagedResult.getContent();
        } else
            return new ArrayList<>();
    }

    public List<VoiceEntity> getVoiceByStatus(StatusTypes status) {
        return voiceRepo.findByStatus(status);
    }


    //Method to be called on post mapping of create voice
    public VoiceEntity recordVoice(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, VoiceEntity voice) {

        if (voice.getDateCreated() == null) {
            voice.setDateCreated(new Date());
        }
        if (voice.getDateModified() == null) {
            voice.setDateModified(new Date());
        }
        if (voice.getStatus() == null) {
            voice.setStatus(StatusTypes.UNREVIEWED);
        }


        return voiceRepo.save(voice);
    }

    public VoiceEntity getSingleVoiceRecord(Long voiceId) {
        return voiceRepo.findById(voiceId).orElse(null);
    }


    //Fetch recorder's pending tasks
    public ResponseEntity<SentenceToRecordDto> recorderAssignedTasks(Long recorderId, BatchStatus batchStatus, Long batchDetailsId) {
        List<BatchDetailsEntity> batchDetails;
        if (batchDetailsId != null) {
            batchDetails = batchDetailsRepo.findByBatchDetailsIdAndAssignedRecorder(batchDetailsId, recorderId);
        } else {
            batchDetails = batchDetailsRepo.findByAssignedRecorderAndBatchStatus(batchStatus, recorderId);
        }


        Long batchDetailId = null;
        List<TranslatedSentenceItemDto> unrecordedVoiceDto = new ArrayList<>();
        List<TranslatedSentenceItemDto> recordedVoiceTasksDto = new ArrayList<>();
        String language = null;


        if (!batchDetails.isEmpty()) {
            for (BatchDetailsEntity aBatchDetail : batchDetails) {
                batchDetailId = aBatchDetail.getBatchDetailsId();
                language = aBatchDetail.getLanguage().getName();

                List<TranslatedSentenceEntity> unrecordedVoiceTasks = translatedSentenceRepo.findUnrecordedVoiceTasksAndUserId(batchDetailId, recorderId);
                unrecordedVoiceDto = unrecordedVoiceTasks.stream()
                        .map(entity -> TranslatedSentenceItemDto.entityToDto(entity, null, null))
                        .collect(Collectors.toList());

                List<VoiceEntity> recordedVoiceTasks = voiceRepo.findAllRecordedVoiceByBatchDetailsIdAndUserId(recorderId, batchDetailId);
                recordedVoiceTasksDto = recordedVoiceTasks.stream()
                        .map(entity -> {
                            Boolean isAccepted;
                            if (entity.getStatus() == StatusTypes.UNREVIEWED)
                                isAccepted = null;
                            else {
                                isAccepted = entity.getStatus() == StatusTypes.APPROVED;
                            }

                            String presignedUrl = amazonClient.generatePresignedUrl(entity.getFileUrl());
                            entity.setPresignedUrl(presignedUrl);
                            return TranslatedSentenceItemDto.voiceEntityToDto(entity, null, isAccepted);
                        })
                        .collect(Collectors.toList());

                if (!unrecordedVoiceTasks.isEmpty())
                    break;
            }
        }

        SentenceToRecordDto sentenceToRecordDto = new SentenceToRecordDto();
        sentenceToRecordDto.setBatchDetailsId(batchDetailId);
        sentenceToRecordDto.setLanguage(language);
        sentenceToRecordDto.setRecordedSentences(recordedVoiceTasksDto);
        sentenceToRecordDto.setUnrecordedSentences(unrecordedVoiceDto);

        return ResponseEntity.ok(sentenceToRecordDto);
    }

    public ResponseMessage approveVoiceRecording(Long voiceId) {
        VoiceEntity voiceEntity = voiceRepo.findById(voiceId).orElse(null);
        if (voiceEntity == null) {
            throw new NotFoundException("Voice not found");
        }
        voiceEntity.setStatus(StatusTypes.APPROVED);
        voiceEntity.setApproved(YesNo.YES);
        VoiceEntity updatedVoiceEntity = voiceRepo.save(voiceEntity);
        Long batchDetailsId = voiceEntity.getTranslatedSentence().getBatchDetailsId();
        Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(batchDetailsId);


        TranslatedSentenceLogsEntity translatedSentenceLogs = getTranslatedSentenceLogsEntity(updatedVoiceEntity.getTranslatedSentence());
        translatedSentenceLogs.setDateAudioModerated(new Date());
        translatedSentenceLogsRepo.save(translatedSentenceLogs);

        if (optionalBatchDetails.isPresent()) {
            BatchDetailsEntity batchDetails = optionalBatchDetails.get();
            Long userId = voiceEntity.getUserId();

            List<BatchDetailsUserAssignment> userAssignmentList =
                    batchDetailsUserAssigmentRepo.findByUserIdAndBatchRoleAndBatchDetails_BatchDetailsId(userId, UserBatchRole.AUDIO_RECORDER, batchDetails.getBatchDetailsId());
            if (!userAssignmentList.isEmpty()) {
                for (BatchDetailsUserAssignment userAssignment : userAssignmentList) {
                    int approved = userAssignment.getAudioApproved() != null ? userAssignment.getAudioApproved() : 0;
                    userAssignment.setAudioApproved(approved + 1);

                    batchDetailsUserAssigmentRepo.save(userAssignment);
                }
            }
        }

        return new ResponseMessage("Voice recording Approved");
    }

    public ResponseMessage rejectVoiceRecording(Long voiceId, String rejectionReason) {
        VoiceEntity voiceEntity = voiceRepo.findById(voiceId).orElse(null);
        if (voiceEntity == null) {
            throw new NotFoundException("Voice not found");
        }

        voiceEntity.setRejectionReason(rejectionReason);
        voiceEntity.setStatus(StatusTypes.REJECTED);
        voiceEntity.setApproved(YesNo.NO);
        VoiceEntity updatedVoiceEntity = voiceRepo.save(voiceEntity);

        Long batchDetailsId = voiceEntity.getTranslatedSentence().getBatchDetailsId();
        Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(batchDetailsId);

        if (optionalBatchDetails.isPresent()) {
            BatchDetailsEntity batchDetails = optionalBatchDetails.get();
            Long userId = voiceEntity.getUserId();

            List<BatchDetailsUserAssignment> userAssignmentList =
                    batchDetailsUserAssigmentRepo.findByUserIdAndBatchRoleAndBatchDetails_BatchDetailsId(userId, UserBatchRole.AUDIO_RECORDER, batchDetails.getBatchDetailsId());
            if (!userAssignmentList.isEmpty()) {
                for (BatchDetailsUserAssignment userAssignment : userAssignmentList) {
                    int rejected = userAssignment.getAudioRejected() != null ? userAssignment.getAudioRejected() : 0;
                    userAssignment.setAudioRejected(rejected + 1);

                    batchDetailsUserAssigmentRepo.save(userAssignment);
                }
            }

        }

        return new ResponseMessage("Voice recording Rejected");
    }

    public ResponseMessage expertApprovedVoiceRecording(Long voiceId) {
        VoiceEntity voiceEntity = voiceRepo.findById(voiceId).orElse(null);
        if (voiceEntity == null) {
            throw new NotFoundException("Voice not found");
        }

        User currentUser = authenticationService.getCurrentUser();
        if (currentUser != null && !Objects.equals(voiceEntity.getExpertUserId(), currentUser.getUserId())) {
            throw new BadRequestException("You are not assigned as an expert for this voice");
        }

        voiceEntity.setExpertReviewedAt(new Date());
        voiceEntity.setStatus(StatusTypes.APPROVED);
        voiceEntity.setApproved(YesNo.YES);
        voiceEntity.setExpertApproved(YesNo.YES);
        voiceRepo.save(voiceEntity);

        Long batchDetailsId = voiceEntity.getBatchDetailsId();
        Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(batchDetailsId);

        if (optionalBatchDetails.isPresent()) {
            BatchDetailsEntity batchDetails = optionalBatchDetails.get();
            Long userId = voiceEntity.getUserId();

            List<BatchDetailsUserAssignment> userAssignmentList =
                    batchDetailsUserAssigmentRepo.findByUserIdAndBatchRoleAndBatchDetails_BatchDetailsId(userId, UserBatchRole.AUDIO_RECORDER, batchDetails.getBatchDetailsId());
            if (!userAssignmentList.isEmpty()) {
                for (BatchDetailsUserAssignment userAssignment : userAssignmentList) {
                    int approved = userAssignment.getAudioExpertApproved() != null ? userAssignment.getAudioExpertApproved() : 0;
                    userAssignment.setAudioExpertApproved(approved + 1);

                    batchDetailsUserAssigmentRepo.save(userAssignment);
                }
            }

        }

        return new ResponseMessage("Voice recording Approved");
    }


    public ResponseMessage expertRejectVoiceRecording(Long voiceId, String reason) {
        VoiceEntity voiceEntity = voiceRepo.findById(voiceId).orElse(null);
        if (voiceEntity == null) {
            throw new NotFoundException("Voice not found");
        }

        if (reason == null || reason.isBlank())
            throw new BadRequestException("Rejection reason is required");

        User currentUser = authenticationService.getCurrentUser();
        if (currentUser != null)
            voiceEntity.setExpertUserId(currentUser.getUserId());

        voiceEntity.setExpertReviewedAt(new Date());
        voiceEntity.setExpertApproved(YesNo.NO);
        voiceEntity.setRejectionReason(reason);
        voiceRepo.save(voiceEntity);

        Long batchDetailsId = voiceEntity.getBatchDetailsId();
        Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(batchDetailsId);

        if (optionalBatchDetails.isPresent()) {
            BatchDetailsEntity batchDetails = optionalBatchDetails.get();
            Long userId = voiceEntity.getUserId();

            List<BatchDetailsUserAssignment> userAssignmentList =
                    batchDetailsUserAssigmentRepo.findByUserIdAndBatchRoleAndBatchDetails_BatchDetailsId(userId, UserBatchRole.AUDIO_RECORDER, batchDetails.getBatchDetailsId());
            if (!userAssignmentList.isEmpty()) {
                for (BatchDetailsUserAssignment userAssignment : userAssignmentList) {
                    int rejected = userAssignment.getAudioExpertRejected() != null ? userAssignment.getAudioExpertRejected() : 0;
                    userAssignment.setAudioExpertRejected(rejected + 1);

                    batchDetailsUserAssigmentRepo.save(userAssignment);
                }
            }

        }

        return new ResponseMessage("Voice recording Rejected");
    }

    public List<TranslatedSentenceEntity> findVoiceTasks(Long batchDetailId) {
        return translatedSentenceRepo.findUnrecordedVoiceTasks(batchDetailId);
    }

    private TranslatedSentenceLogsEntity getTranslatedSentenceLogsEntity(TranslatedSentenceEntity translatedSentence) {
        TranslatedSentenceLogsEntity translatedSentenceLogs = translatedSentenceLogsRepo.findByTranslatedSentence(translatedSentence);
        if (translatedSentenceLogs == null) {
            translatedSentenceLogs = new TranslatedSentenceLogsEntity();
            translatedSentenceLogs.setTranslatedSentence(translatedSentence);
        }
        return translatedSentenceLogs;
    }
}