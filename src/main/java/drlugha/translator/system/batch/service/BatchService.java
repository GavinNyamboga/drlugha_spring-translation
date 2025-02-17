package drlugha.translator.system.batch.service;

import drlugha.translator.configs.AmazonClient;
import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.shared.enums.DeletionStatus;
import drlugha.translator.shared.enums.StatusTypes;
import drlugha.translator.shared.enums.YesNo;
import drlugha.translator.shared.exception.BadRequestException;
import drlugha.translator.shared.exception.GeneralException;
import drlugha.translator.shared.exception.NotFoundException;
import drlugha.translator.system.batch.dto.*;
import drlugha.translator.system.batch.enums.*;
import drlugha.translator.system.batch.model.BatchDetailsEntity;
import drlugha.translator.system.batch.model.BatchDetailsStatsEntity;
import drlugha.translator.system.batch.model.BatchDetailsUserAssignment;
import drlugha.translator.system.batch.model.BatchEntity;
import drlugha.translator.system.batch.repository.BatchDetailsRepository;
import drlugha.translator.system.batch.repository.BatchDetailsStatsRepository;
import drlugha.translator.system.batch.repository.BatchDetailsUserAssigmentRepo;
import drlugha.translator.system.batch.repository.BatchRepository;
import drlugha.translator.system.language.model.Language;
import drlugha.translator.system.language.repository.LanguageRepository;
import drlugha.translator.system.sentence.dto.*;
import drlugha.translator.system.sentence.model.ModeratorCommentEntity;
import drlugha.translator.system.sentence.model.Sentence;
import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.system.sentence.repository.ModeratorCommentRepo;
import drlugha.translator.system.sentence.repository.SentenceRepository;
import drlugha.translator.system.sentence.repository.TranslatedSentenceRepository;
import drlugha.translator.system.sentence.service.SentenceService;
import drlugha.translator.system.stats.dto.BatchDetailsStatsDto;
import drlugha.translator.system.user.dto.UserDetailDTO;
import drlugha.translator.system.user.model.User;
import drlugha.translator.system.user.repository.UserRepository;
import drlugha.translator.system.voice.model.VoiceEntity;
import drlugha.translator.system.voice.repository.VoiceRepository;
import drlugha.translator.system.voice.service.VoiceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class BatchService {

    private final BatchRepository batchRepo;

    private final BatchDetailsRepository batchDetailsRepo;

    private final SentenceService sentenceService;

    private final SentenceRepository sentenceRepository;

    private final TranslatedSentenceRepository translatedSentenceRepo;

    private final VoiceRepository voiceRepo;

    private final LanguageRepository languageRepository;

    private final UserRepository userRepository;

    private final ModeratorCommentRepo moderatorCommentRepo;

    private final TranslatedSentenceRepository translatedSentenceRepository;

    private final BatchDetailsStatsRepository batchDetailsStatsRepository;

    private final VoiceService voiceService;

    private final AmazonClient amazonClient;

    private final BatchDetailsUserAssigmentRepo batchDetailsUserAssigmentRepo;

    CreatePrefixedBatchDTO prefixedBatchDto;

    private final ExecutorService downloadExecutor = Executors.newFixedThreadPool(10);

    public BatchService(BatchRepository batchRepo, BatchDetailsRepository batchDetailsRepo,
                        SentenceService sentenceService, SentenceRepository sentenceRepository,
                        TranslatedSentenceRepository translatedSentenceRepo, VoiceRepository voiceRepo,
                        LanguageRepository languageRepository, UserRepository userRepository,
                        ModeratorCommentRepo moderatorCommentRepo, TranslatedSentenceRepository translatedSentenceRepository,
                        BatchDetailsStatsRepository batchDetailsStatsRepository, VoiceService voiceService, AmazonClient amazonClient, BatchDetailsUserAssigmentRepo batchDetailsUserAssigmentRepo) {
        this.batchRepo = batchRepo;
        this.batchDetailsRepo = batchDetailsRepo;
        this.sentenceService = sentenceService;
        this.sentenceRepository = sentenceRepository;
        this.translatedSentenceRepo = translatedSentenceRepo;
        this.voiceRepo = voiceRepo;
        this.languageRepository = languageRepository;
        this.userRepository = userRepository;
        this.moderatorCommentRepo = moderatorCommentRepo;
        this.translatedSentenceRepository = translatedSentenceRepository;
        this.batchDetailsStatsRepository = batchDetailsStatsRepository;
        this.voiceService = voiceService;
        this.amazonClient = amazonClient;
        this.batchDetailsUserAssigmentRepo = batchDetailsUserAssigmentRepo;
    }

    public ResponseMessage addBatch(BatchDTO batchDto) {
        BatchEntity batchEntity = batchDto.dtoToEntity();
        batchEntity.setFromFeedback(YesNo.NO);
        batchEntity.setBatchOrigin(batchDto.getBatchOrigin());
        BatchEntity batch = this.batchRepo.save(batchEntity);
        return this.sentenceService.addSentences(batchDto.getSentences(), batch.getBatchNo());
    }

    public BatchEntity editBatch(BatchDTO batchDto) {
        if (batchDto.getBatchNo() == null)
            throw new BadRequestException("Please provide old batch no");
        if (batchDto.getSource() == null || batchDto.getSource().isBlank())
            throw new BadRequestException("Please provide source");
        if (batchDto.getDescription() == null || batchDto.getDescription().isBlank())
            throw new BadRequestException("Please provide description");
        if (batchDto.getLinkUrl() == null || batchDto.getLinkUrl().isBlank())
            throw new BadRequestException("Please provide link url");
        Optional<BatchEntity> optionalOldBatch = this.batchRepo.findById(batchDto.getBatchNo());
        if (optionalOldBatch.isEmpty())
            throw new BadRequestException("Sorry, the batch you are trying to edit was not found.");
        BatchEntity batch = optionalOldBatch.get();
        batch.setSource(batchDto.getSource());
        batch.setDescription(batchDto.getDescription());
        batch.setLinkUrl(batch.getLinkUrl());
        this.batchRepo.save(batch);
        return batch;
    }

    @Transactional
    public ResponseMessage deleteBatch(Long batchNo) {
        if (batchNo == null)
            throw new BadRequestException("Please provide batch no.");
        Optional<BatchEntity> batch = this.batchRepo.findById(batchNo);
        if (batch.isEmpty())
            throw new BadRequestException("Sorry, the batch you are trying to delete was not found.");
        this.sentenceRepository.deleteAllByBatchNo(batchNo);
        this.translatedSentenceRepo.deleteAllByBatchNumber(batchNo);
        this.batchDetailsStatsRepository.deleteAllByBatchId(batchNo);
        this.batchDetailsRepo.deleteAllByBatchId(batchNo);
        this.batchRepo.deleteById(batchNo);
        return new ResponseMessage("Batch successfully deleted");
    }

    @Transactional
    public BatchDetailsEntity addBatchDetails(AddBatchDetailsDTO batchDetailsDto, Long batchNo) {
        if (batchDetailsDto == null)
            throw new BadRequestException("Please provide translated by id and language");
        if (batchDetailsDto.getTranslatedById() == null)
            throw new BadRequestException("Please provide translated by id");
        if (batchDetailsDto.getLanguage() == null)
            throw new BadRequestException("Please provide language");
        Optional<Language> optionalLanguageEntity = this.languageRepository.findById(batchDetailsDto.getLanguage());
        if (optionalLanguageEntity.isEmpty())
            throw new BadRequestException("Language does not exist");
        Optional<User> user = this.userRepository.findById(batchDetailsDto.getTranslatedById());
        if (user.isEmpty())
            throw new BadRequestException("User not found");
        Optional<BatchEntity> optionalBatch = this.batchRepo.findById(batchNo);
        if (optionalBatch.isEmpty())
            throw new BadRequestException("Batch not found");
        BatchEntity batchEntity = optionalBatch.get();
        if (batchEntity.getBatchType() == BatchType.AUDIO && !Objects.equals(batchDetailsDto.getLanguage(), batchEntity.getAudioLanguage().getLanguageId()))
            throw new BadRequestException("Wrong language for the Audio Batch. The correct language is " + batchEntity.getAudioLanguage().getName());
        BatchDetailsEntity batchDetails = new BatchDetailsEntity();
        batchDetails.setTranslatedById(batchDetailsDto.getTranslatedById());
        batchDetails.setTranslatedBy(user.get());
        batchDetails.setLanguage(optionalLanguageEntity.get());
        batchDetails.setBatchId(batchEntity.getBatchNo());
        batchDetails.setBatchStatus(BatchStatus.ASSIGNED_TRANSLATOR);
        batchDetails = this.batchDetailsRepo.save(batchDetails);

        //create a record on the new table
        createBatchUserAssignment(batchDetailsDto.getTranslatedById(), batchDetails, batchEntity.getBatchNo(), UserBatchRole.TEXT_TRANSLATOR);

        Optional<BatchDetailsStatsEntity> batchDetailsStats = this.batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetails.getBatchDetailsId());
        if (batchDetailsStats.isEmpty()) {
            BatchDetailsStatsEntity batchDetailsStatsEntity = new BatchDetailsStatsEntity();
            batchDetailsStatsEntity.setBatchDetails(batchDetails);
            this.batchDetailsStatsRepository.save(batchDetailsStatsEntity);
        }
        return batchDetails;
    }

    public List<BatchDetailsDTO> getBatchDetailsByBatch(Long batchId) {
        List<BatchDetailsEntity> batchDetails = this.batchDetailsRepo.findByBatchId(batchId);
        List<BatchDetailsDTO> batchDtoList = new ArrayList<>();
        for (BatchDetailsEntity batchDetails1 : batchDetails) {
            log.info("BATCH DETAILS: {} BATCH ID {}", batchDetails1.getBatchDetailsId(), batchDetails1.getBatchId());
            BatchDetailsDTO batchDetailsDto = (new BatchDetailsDTO()).entityToDto(batchDetails1);
            batchDtoList.add(batchDetailsDto);
        }
        return batchDtoList;
    }

    public BatchDetailsEntity editBatchDetailsStatus(BatchDetailsEntity batchDetails, Long batchDetailsId) {
        BatchDetailsEntity batchDetails1 = batchDetailsRepo.findById(batchDetailsId).get();
        if (Objects.nonNull(batchDetails.getBatchStatus()))
            batchDetails1.setBatchStatus(batchDetails.getBatchStatus());
        return this.batchDetailsRepo.save(batchDetails1);
    }

    public BatchDetailsEntity assignTextVerifier(BatchDetailsEntity batchDetails, Long batchDetailsId) {
        BatchDetailsEntity batchDetails1 = this.batchDetailsRepo.findById(batchDetailsId).get();
        if (Objects.nonNull(batchDetails.getTranslationVerifiedById()))
            batchDetails1.setTranslationVerifiedById(batchDetails.getTranslationVerifiedById());
        batchDetails1.setBatchStatus(BatchStatus.ASSIGNED_TEXT_VERIFIER);
        return this.batchDetailsRepo.save(batchDetails1);
    }

    public ResponseMessage createPrefixedBatch(CreatePrefixedBatchDTO batchDto) {
        // Prefix the description directly, no need to get it from the DTO
        String prefixedDescription = generatePrefixedDescription("Rereviewed batch");

        // Create and save the batch entity
        BatchEntity batchEntity = new BatchEntity();
        batchEntity.setDescription(prefixedDescription);
        // Exclude uploaderId handling

        // Save the batch entity
        BatchEntity savedBatch = batchRepo.save(batchEntity);

        // Add sentences to the batch
        return sentenceService.addSentences(prefixedBatchDto.getSentences(), savedBatch.getBatchNo());
    }


    private String generatePrefixedDescription(String originalDescription) {
        // Static prefix statement
        String staticPrefix = "This batch has been reviewed: ";

        // Return the final prefixed description
        return staticPrefix + originalDescription;
    }

    @Transactional
    public BatchDetailsEntity assignExpertReviewer(BatchDetailsEntity batchDetails, Long batchDetailsId) {
        BatchDetailsEntity batchDetails1 = this.batchDetailsRepo.findById(batchDetailsId).get();
        int noOfSentencesToReview = (int) Math.ceil(0.1D * batchDetails1.getTranslatedSentence().size());
        List<TranslatedSentenceEntity> sentencesToReview = batchDetails1.getTranslatedSentence().subList(0, noOfSentencesToReview);
        List<Long> translatedSentencesToReviewIds = sentencesToReview.stream().map(TranslatedSentenceEntity::getTranslatedSentenceId)
                .collect(Collectors.toList());
        this.translatedSentenceRepo.assignSentencesToExpertReviewer(translatedSentencesToReviewIds);
        if (Objects.nonNull(batchDetails.getSecondReviewerId()))
            batchDetails1.setSecondReviewerId(batchDetails.getSecondReviewerId());
        batchDetails1.setBatchStatus(BatchStatus.ASSIGNED_EXPERT_REVIEWER);
        return this.batchDetailsRepo.save(batchDetails1);
    }

    public ResponseEntity assignRecorder(BatchDetailsEntity batchDetails, Long batchDetailsId) {
        Optional<BatchDetailsEntity> optionalBatchDetails = this.batchDetailsRepo.findById(batchDetailsId);
        if (optionalBatchDetails.isEmpty())
            return ResponseEntity.badRequest().body(new ResponseMessage("Batch details does not exist"));
        if (batchDetails.getRecordedById() == null)
            return ResponseEntity.badRequest().body(new ResponseMessage("Please provide user id of the recorder"));
        BatchDetailsEntity batchDetailsEntity = optionalBatchDetails.get();
        if (batchDetailsEntity.getBatch().getBatchType() == BatchType.AUDIO)
            return ResponseEntity.badRequest().body(new ResponseMessage("Audio batches cannot be assigned a recorder"));
        batchDetailsEntity.setRecordedById(batchDetails.getRecordedById());
        batchDetailsEntity.setBatchStatus(BatchStatus.ASSIGNED_RECORDER);
        return ResponseEntity.ok().body(this.batchDetailsRepo.save(batchDetailsEntity));
    }

    public ResponseEntity assignAudioVerifier(BatchDetailsEntity batchDetails, Long batchDetailsId) {
        Optional<BatchDetailsEntity> optionalBatchDetails = this.batchDetailsRepo.findById(batchDetailsId);
        if (optionalBatchDetails.isEmpty())
            return ResponseEntity.badRequest().body(new ResponseMessage("Batch details does not exist"));
        if (batchDetails.getAudioVerifiedById() == null)
            return ResponseEntity.badRequest().body(new ResponseMessage("Please provide user id of the verifier"));
        BatchDetailsEntity batchDetailsEntity = optionalBatchDetails.get();
        if (batchDetailsEntity.getBatch().getBatchType() == BatchType.AUDIO)
            return ResponseEntity.badRequest().body(new ResponseMessage("Audio batches cannot be assigned an audio verifier"));
        batchDetailsEntity.setAudioVerifiedById(batchDetails.getAudioVerifiedById());
        batchDetailsEntity.setBatchStatus(BatchStatus.ASSIGNED_AUDIO_VERIFIER);
        return ResponseEntity.ok().body(this.batchDetailsRepo.save(batchDetailsEntity));
    }

    public ResponseEntity<SentenceToReviewDto> reviewerAssignedTasks(Long reviewerId, BatchStatus batchStatus, Long batchDetailsId) {
        List<BatchDetailsEntity> batchDetails;
        if (batchDetailsId != null) {
            batchDetails = this.batchDetailsRepo.findByTranslationVerifiedByIdAndBatchDetailsId(reviewerId, batchDetailsId);
        } else {
            batchDetails = this.batchDetailsRepo.findByTranslationVerifiedByIdAndBatchStatus(reviewerId, batchStatus);
        }
        batchDetailsId = null;
        List<TranslatedSentenceItemDto> unreviewedSentencesDto = new ArrayList<>();
        List<TranslatedSentenceItemDto> reviewedSentencesDto = new ArrayList<>();
        String language = null;
        String batchType = null;
        if (!batchDetails.isEmpty())
            for (BatchDetailsEntity aBatchDetail : batchDetails) {
                batchDetailsId = aBatchDetail.getBatchDetailsId();
                language = aBatchDetail.getLanguage().getName();
                batchType = aBatchDetail.getBatch().getBatchType().getName();
                List<TranslatedSentenceEntity> unreviewedSentences = this.translatedSentenceRepo.findUnreviewedByTranslationVerifiedByIdAndBatchDetailsId(reviewerId, batchDetailsId);
                List<TranslatedSentenceEntity> reviewedSentences = this.translatedSentenceRepo.findReviewedByTranslationVerifiedByIdAndBatchDetailsId(reviewerId, batchDetailsId);
                unreviewedSentencesDto = getTranslatedSentenceItemDtos(unreviewedSentences, Boolean.FALSE, null);
                reviewedSentencesDto = getTranslatedSentenceItemDtos(reviewedSentences, Boolean.TRUE, null);
                if (!unreviewedSentences.isEmpty())
                    break;
            }
        SentenceToReviewDto sentenceToReviewDto = new SentenceToReviewDto();
        sentenceToReviewDto.setBatchDetailsId(batchDetailsId);
        sentenceToReviewDto.setLanguage(language);
        sentenceToReviewDto.setUnreviewedSentences(unreviewedSentencesDto);
        sentenceToReviewDto.setReviewedSentences(reviewedSentencesDto);
        sentenceToReviewDto.setBatchType(batchType);
        return ResponseEntity.ok(sentenceToReviewDto);
    }

    public ResponseEntity<SentenceToReviewDto> expertReviewerAssignedTasks(Long reviewerId, BatchStatus batchStatus, Long batchDetailsId) {
        List<BatchDetailsEntity> batchDetails;
        if (batchDetailsId != null) {
            batchDetails = this.batchDetailsRepo.findBySecondReviewerIdAndBatchDetailsId(reviewerId, batchDetailsId);
        } else {
            batchDetails = this.batchDetailsRepo.findBySecondReviewerIdAndBatchStatus(reviewerId, batchStatus);
        }
        Long batchDetailId = null;
        List<TranslatedSentenceItemDto> unreviewedTranslatedSentencesDto = new ArrayList<>();
        List<TranslatedSentenceItemDto> reviewedTranslatedSentencesDto = new ArrayList<>();
        String language = null;
        String batchType = null;
        if (!batchDetails.isEmpty())
            for (BatchDetailsEntity aBatchDetail : batchDetails) {
                batchDetailId = aBatchDetail.getBatchDetailsId();
                language = aBatchDetail.getLanguage().getName();
                batchType = aBatchDetail.getBatch().getBatchType().getName();
                List<TranslatedSentenceEntity> unreviewedTranslatedSentences = this.translatedSentenceRepo.findExpertReviewersUnreviewedTasks(reviewerId, batchDetailId);
                List<TranslatedSentenceEntity> reviewedTranslatedSentences = this.translatedSentenceRepo.findExpertReviewersReviewedTasks(reviewerId, batchDetailId);
                unreviewedTranslatedSentencesDto = getTranslatedSentenceItemDtos(unreviewedTranslatedSentences, null, Boolean.FALSE);
                reviewedTranslatedSentencesDto = getTranslatedSentenceItemDtos(reviewedTranslatedSentences, null, Boolean.TRUE);
                if (!unreviewedTranslatedSentences.isEmpty())
                    break;
            }
        SentenceToReviewDto sentenceToReviewDto = new SentenceToReviewDto();
        sentenceToReviewDto.setBatchDetailsId(batchDetailId);
        sentenceToReviewDto.setLanguage(language);
        sentenceToReviewDto.setUnreviewedSentences(unreviewedTranslatedSentencesDto);
        sentenceToReviewDto.setReviewedSentences(reviewedTranslatedSentencesDto);
        sentenceToReviewDto.setBatchType(batchType);
        return ResponseEntity.ok(sentenceToReviewDto);
    }

    public BatchInfoDTO getTranslatorBatchDetails(Long userId) {
        List<BatchDetailsEntity> translationBatchDetails = this.batchDetailsRepo.findAllByTranslatedByIdAndBatch_BatchType(userId, BatchType.TEXT);
        List<BatchInfoItemDTO> sortedTranslationBatchDetails = getSortedTranslationBatchDetails(translationBatchDetails);
        List<BatchDetailsEntity> transcriptionBatchDetails = this.batchDetailsRepo.findAllByTranslatedByIdAndBatch_BatchType(userId, BatchType.AUDIO);
        List<BatchInfoItemDTO> sortedTranscriptionBatchDetails = getSortedTranslationBatchDetails(transcriptionBatchDetails);
        return new BatchInfoDTO(sortedTranslationBatchDetails, sortedTranscriptionBatchDetails);
    }

    public BatchInfoDTO getReviewerBatchDetails(Long userId) {
        List<BatchDetailsEntity> translationBatchDetails = this.batchDetailsRepo.findAllByTranslationVerifiedByIdAndBatch_BatchType(userId, BatchType.TEXT);
        List<BatchInfoItemDTO> sortedTranslationBatchDetails = getSortedReviewerBatchDetails(translationBatchDetails);
        List<BatchDetailsEntity> transcriptionBatchDetails = this.batchDetailsRepo.findAllByTranslationVerifiedByIdAndBatch_BatchType(userId, BatchType.AUDIO);
        List<BatchInfoItemDTO> sortedTranscriptionBatchDetails = getSortedReviewerBatchDetails(transcriptionBatchDetails);
        return new BatchInfoDTO(sortedTranslationBatchDetails, sortedTranscriptionBatchDetails);
    }

    public BatchInfoDTO getExpertReviewerBatchDetails(Long userId) {
        List<BatchDetailsEntity> translationBatchDetails = this.batchDetailsRepo.findAllBySecondReviewerIdAndBatch_BatchType(userId, BatchType.TEXT);
        List<BatchInfoItemDTO> sortedTranslationBatchDetails = getSortedExpertReviewerBatchDetails(translationBatchDetails);
        List<BatchDetailsEntity> transcriptionBatchDetails = this.batchDetailsRepo.findAllBySecondReviewerIdAndBatch_BatchType(userId, BatchType.AUDIO);
        List<BatchInfoItemDTO> sortedTranscriptionBatchDetails = getSortedExpertReviewerBatchDetails(transcriptionBatchDetails);
        return new BatchInfoDTO(sortedTranslationBatchDetails, sortedTranscriptionBatchDetails);
    }

    public BatchInfoDTO getAudioRecorderBatchDetails(Long userId) {
        List<BatchDetailsEntity> batchDetails = this.getBatchDetailsByUserRole(userId, UserBatchRole.AUDIO_RECORDER, null);
        List<BatchInfoItemDTO> sortedBatchDetails = batchDetails.stream().map(element -> {
            Integer rejectedAudios = this.voiceRepo.countAllByStatusAndTranslatedSentenceBatchDetailsId(StatusTypes.REJECTED, element.getBatchDetailsId());
            BatchInfoItemDTO batchInfoItemDTO = new BatchInfoItemDTO(element);
            if (batchInfoItemDTO.getAudioRecorded()) {
                batchInfoItemDTO.setPendingSentences(rejectedAudios);
                batchInfoItemDTO.setAudioRecorded(rejectedAudios <= 0);
            }
            return batchInfoItemDTO;
        }).sorted((e1, e2) -> (!e1.getAudioRecorded() && e2.getAudioRecorded()) ? -1 : ((e1.getAudioRecorded() && !e2.getAudioRecorded()) ? 1 : 0)).collect(Collectors.toList());
        return new BatchInfoDTO(sortedBatchDetails, null);
    }

    public BatchInfoDTO getAudioReviewerBatchDetails(Long userId) {
        List<BatchDetailsEntity> batchDetails = this.getBatchDetailsByUserRole(userId, UserBatchRole.AUDIO_VERIFIER, null);
        List<BatchInfoItemDTO> sortedBatchDetails = batchDetails.stream().map(element -> {
            Integer unreviewedAudios = this.voiceRepo.countAllByStatusAndTranslatedSentenceBatchDetailsId(StatusTypes.UNREVIEWED, element.getBatchDetailsId());
            BatchInfoItemDTO batchInfoItemDTO = new BatchInfoItemDTO(element);
            if (batchInfoItemDTO.getAudioReviewed())
                batchInfoItemDTO.setAudioRecorded(unreviewedAudios <= 0);
            batchInfoItemDTO.setPendingSentences(unreviewedAudios);
            return batchInfoItemDTO;
        }).sorted((e1, e2) -> (!e1.getAudioReviewed() && e2.getAudioReviewed()) ? -1 : ((e1.getAudioReviewed() && !e2.getAudioReviewed()) ? 1 : 0)).collect(Collectors.toList());
        return new BatchInfoDTO(sortedBatchDetails, null);
    }

    public BatchInfoDTO getExpertAudioReviewerBatchDetails(Long userId) {
        List<BatchDetailsEntity> batchesDetailsToVerify = this.getBatchDetailsByUserRole(userId, UserBatchRole.EXPERT_AUDIO_REVIEWER, null);

        List<BatchInfoItemDTO> sortedBatchDetails = batchesDetailsToVerify.stream().map(element -> {
            Integer unreviewedAudios = this.voiceRepo.countAllByStatusAndTranslatedSentenceBatchDetailsId(StatusTypes.UNREVIEWED, element.getBatchDetailsId());
            BatchInfoItemDTO batchInfoItemDTO = new BatchInfoItemDTO(element);
            if (batchInfoItemDTO.getAudioReviewed())
                batchInfoItemDTO.setAudioRecorded(unreviewedAudios <= 0);
            batchInfoItemDTO.setPendingSentences(unreviewedAudios);
            return batchInfoItemDTO;
        }).sorted(
                (e1, e2) ->
                        (!e1.getAudioReviewed() && e2.getAudioReviewed()) ? -1 : ((e1.getAudioReviewed() && !e2.getAudioReviewed()) ? 1 : 0)
        ).collect(Collectors.toList());
        return new BatchInfoDTO(sortedBatchDetails, null);
    }

    List<BatchDetailsEntity> getBatchDetailsByUserRole(Long userId, UserBatchRole userBatchRole, Long batchDetailsId) {
        List<BatchDetailsUserAssignment> userAssignments;
        if (batchDetailsId != null)
            userAssignments = batchDetailsUserAssigmentRepo.findByUserIdAndBatchRoleAndBatchDetails_BatchDetailsId(userId, userBatchRole, batchDetailsId);
        else
            userAssignments = batchDetailsUserAssigmentRepo.findByUserIdAndBatchRole(userId, userBatchRole);

        List<BatchDetailsEntity> batchDetailsEntities = new ArrayList<>();
        for (BatchDetailsUserAssignment userAssignment : userAssignments) {
            BatchDetailsEntity batchDetailsEntity = userAssignment.getBatchDetails();
            if (batchDetailsEntity != null && userAssignment.getBatchDetailsId() != null) {
                batchDetailsEntity = batchDetailsRepo.findById(batchDetailsEntity.getBatchDetailsId()).orElse(null);
            }
            if (batchDetailsEntity != null)
                batchDetailsEntities.add(batchDetailsEntity);
        }
        return batchDetailsEntities;
    }

    public BatchInfoDTO getBatchDetailsByTask(Long userId, Task task) {
        log.info("TASK....{}", task);
        switch (task) {
            case TRANSLATION:
                return getTranslatorBatchDetails(userId);
            case REVIEW:
                return getReviewerBatchDetails(userId);
            case EXPERT_REVIEW:
                return getExpertReviewerBatchDetails(userId);
            case AUDIO_RECORDING:
                return getAudioRecorderBatchDetails(userId);
            case AUDIO_REVIEWING:
                return getAudioReviewerBatchDetails(userId);
            case AUDIO_EXPERT_REVIEWING:
                return getExpertAudioReviewerBatchDetails(userId);
        }
        return new BatchInfoDTO();
    }

    public CompletedSentencesDto getCompletedSentencesPerBatchDetails(Long batchDetailsId) {
        if (batchDetailsId == null)
            throw new BadRequestException("Please provide batch details id");

        Optional<BatchDetailsEntity> optionalBatchDetails = this.batchDetailsRepo.findById(batchDetailsId);
        if (optionalBatchDetails.isEmpty())
            throw new BadRequestException("Batch details not found");

        BatchDetailsEntity batchDetails = optionalBatchDetails.get();
        String language = batchDetails.getLanguage().getName();
        Integer numberOfAllSentences = this.sentenceRepository.countAllByBatchNo(batchDetails.getBatch().getBatchNo());

        List<VoiceEntity> approvedVoices = this.voiceRepo.findAllByStatusAndTranslatedSentenceBatchDetailsId(StatusTypes.APPROVED, batchDetailsId);
        List<CompletedSentenceItemDto> completedSentenceList = approvedVoices.stream().map(voice -> {
            String presignedUrl = this.amazonClient.generatePresignedUrl(voice.getFileUrl());
            voice.setFileUrl(presignedUrl);

            TranslatedSentenceEntity translatedSentence = voice.getTranslatedSentence();
            UserDetailDTO recordedBy = translatedSentence.getRecordedBy() != null
                    ? new UserDetailDTO().toDto(translatedSentence.getRecordedBy())
                    : null;

            return new CompletedSentenceItemDto(voice, recordedBy);
        }).collect(Collectors.toList());

        CompletedSentencesDto completedSentencesDto = new CompletedSentencesDto();
        completedSentencesDto.setBatchDetailsId(batchDetailsId);
        completedSentencesDto.setLanguage(language);
        completedSentencesDto.setNumberOfSentences(numberOfAllSentences);
        completedSentencesDto.setNumberOfCompletedSentences(approvedVoices.size());
        completedSentencesDto.setCompletedSentences(completedSentenceList);

        return completedSentencesDto;
    }


    public ResponseEntity<List<BatchInfoStatsDTO>> getBatchStats() {
        List<BatchDetailsEntity> batchDetailsList = this.batchDetailsRepo.findAll(
                Sort.by(Sort.Direction.ASC, "batch.source"));
        List<BatchInfoStatsDTO> batchInfoStats = batchDetailsList.stream().map(batchDetail -> {
            Integer numberOfAllSentences = this.sentenceRepository.countAllByBatchNo(batchDetail.getBatch().getBatchNo());
            BatchDetailsStatsDto batchDetailsStatsDto = this.translatedSentenceRepo.getBatchDetailsStats(batchDetail.getBatchDetailsId());
            return BatchInfoStatsDTO.entityToDto(batchDetail, numberOfAllSentences, batchDetailsStatsDto);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(batchInfoStats);
    }

    public ResponseEntity<?> getTranslatedSentences(Long batchDetailsId) {
        Logger logger = LoggerFactory.getLogger(BatchService.class);

        logger.info("Entering getTranslatedSentences with batchDetailsId: {}", batchDetailsId);

        if (batchDetailsId == null) {
            logger.error("Batch details id is null");
            return ResponseEntity.badRequest().body(new ResponseMessage("Please provide batch details id"));
        }

        Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(batchDetailsId);
        if (optionalBatchDetails.isEmpty()) {
            logger.error("Batch details not found for id: {}", batchDetailsId);
            return ResponseEntity.notFound().build();
        }

        BatchDetailsEntity batchDetails = optionalBatchDetails.get();
        BatchType batchType = batchDetails.getBatch().getBatchType();
        logger.info("Batch details found for id: {}. BatchType: {}", batchDetailsId, batchType);

        // Retrieve sentences with presigned audio URL
        List<SentenceItemDto> sentenceItems = batchDetailsRepo.getAllSentencesInBatchDetailsV2(Collections.singletonList(batchDetailsId));
        logger.info("Number of sentence items retrieved: {}", sentenceItems.size());

        //List<CompletedSentenceItemDto> sentences = getSentencesWithPresignedAudioUrl(sentenceItems);
        //logger.info("Number of completed sentences with presigned audio URL: {}", sentences.size());

        Map<Long, CompletedSentenceItemDto> completedSentenceItemDtoMap = new HashMap<>();

        for (SentenceItemDto sentence : sentenceItems) {
            CompletedSentenceItemDto completedSentenceItemDto = completedSentenceItemDtoMap.computeIfAbsent(
                    sentence.getTranslatedSentenceId(),
                    id -> {
                        CompletedSentenceItemDto dto = new CompletedSentenceItemDto();
                        dto.setTranslatedSentenceId(id);
                        dto.setSentenceId(sentence.getSentenceId());
                        dto.setSentenceText(sentence.getSentenceText());
                        dto.setTranslatedText(sentence.getTranslatedText());
                        dto.setTranscriptionAudioUrl(sentence.getTranscriptionAudioUrl());
                        dto.setBatchDetailsId(sentence.getBatchDetailsId());
                        dto.setAudioDetails(new ArrayList<>()); // Initialize the list
                        return dto;
                    }
            );
            if (sentence.getAudioUrl() != null) {
                UserDetailDTO userDetailDTO = new UserDetailDTO();
                userDetailDTO.setFileUrl(sentence.getAudioUrl());
                userDetailDTO.setUsername(sentence.getRecordedByUsername());
                userDetailDTO.setUserId(sentence.getRecordedByUserId());
                completedSentenceItemDto.getAudioDetails().add(userDetailDTO);
            }

        }

        List<CompletedSentenceItemDto> sentences = new ArrayList<>(completedSentenceItemDtoMap.values());
        /*for (CompletedSentenceItemDto sentence : sentences) {
            logger.info("Adding recordedBy info for sentence with translatedSentenceId: {}", sentence.getTranslatedSentenceId());
            List<UserDetailDTO> recordedBy = getVoiceDetailsByTranslatedSentenceId(List.of(sentence.getTranslatedSentenceId()));
            if (recordedBy == null) {
                logger.warn("recordedBy is null for translatedSentenceId: {}", sentence.getTranslatedSentenceId());
            }
            UserDetailDTO userDetailDTO = recordedBy != null && !recordedBy.isEmpty() ? recordedBy.get(0) : null;
            sentence.setRecordedBy(userDetailDTO);
        }*/

        CompletedSentencesDto completedSentencesDto = new CompletedSentencesDto();
        completedSentencesDto.setBatchDetailsId(batchDetailsId);
        completedSentencesDto.setBatchType(batchType.getName());
        completedSentencesDto.setBatchDetailsStatus(batchDetails.getBatchStatus().getLabel(batchType));
        completedSentencesDto.setLanguage(batchDetails.getLanguage().getName());
        completedSentencesDto.setNumberOfSentences(sentences.size());
        completedSentencesDto.setNumberOfCompletedSentences(sentences.size());
        completedSentencesDto.setCompletedSentences(sentences);

        logger.info("Returning completed sentences DTO for batchDetailsId: {}", batchDetailsId);
        return ResponseEntity.ok(completedSentencesDto);
    }

    public ResponseEntity<?> downloadBatchDetails(List<Long> batchDetailsIds, boolean excelOnly) throws Exception {
        Logger logger = LoggerFactory.getLogger(BatchService.class);

        // Input validation
        if (batchDetailsIds == null) {
            logger.error("Batch details ids is null");
            return ResponseEntity.badRequest().body(new ResponseMessage("Please provide batch details id"));
        }

        List<BatchDetailsEntity> batchDetailsEntities = batchDetailsRepo.findAllById(batchDetailsIds);
        if (batchDetailsEntities.isEmpty()) {
            throw new BadRequestException("Batch details not found");
        }

        Map<Long, BatchDetailsEntity> batchDetailsEntityMap = batchDetailsEntities.stream()
                .collect(Collectors.toMap(BatchDetailsEntity::getBatchDetailsId, batchDetailsEntity -> batchDetailsEntity));

        // Parallel processing for data retrieval
        CompletableFuture<List<CompletedSentenceItemDto>> sentencesFuture = CompletableFuture.supplyAsync(() -> {
            List<SentenceItemDto> sentenceItems = batchDetailsRepo.getAllSentencesInBatchDetailsV2(batchDetailsIds);
            logger.info("Number of sentence items retrieved: {}", sentenceItems.size());

            Map<Long, CompletedSentenceItemDto> completedSentenceItemDtoMap = new HashMap<>();

            for (SentenceItemDto sentence : sentenceItems) {
                CompletedSentenceItemDto completedSentenceItemDto = completedSentenceItemDtoMap.computeIfAbsent(
                        sentence.getTranslatedSentenceId(),
                        id -> {
                            CompletedSentenceItemDto dto = new CompletedSentenceItemDto();
                            dto.setTranslatedSentenceId(id);
                            dto.setSentenceId(sentence.getSentenceId());
                            dto.setSentenceText(sentence.getSentenceText());
                            dto.setTranslatedText(sentence.getTranslatedText());
                            dto.setTranscriptionAudioUrl(sentence.getTranscriptionAudioUrl());
                            dto.setBatchDetailsId(sentence.getBatchDetailsId());
                            dto.setAudioDetails(new ArrayList<>()); // Initialize the list
                            return dto;
                        }
                );

                if (sentence.getAudioUrl() != null) {
                    UserDetailDTO userDetailDTO = new UserDetailDTO();
                    userDetailDTO.setFileUrl(sentence.getAudioUrl());
                    userDetailDTO.setUsername(sentence.getRecordedByUsername());
                    userDetailDTO.setUserId(sentence.getRecordedByUserId());
                    completedSentenceItemDto.getAudioDetails().add(userDetailDTO);
                }
            }

            return new ArrayList<>(completedSentenceItemDtoMap.values());
        });

        //After fetching sentences, group them by batchId
        CompletableFuture<Map<Long, List<CompletedSentenceItemDto>>> batchSentencesFuture = sentencesFuture
                .thenApplyAsync(sentences ->
                        sentences.stream().collect(Collectors.groupingBy(CompletedSentenceItemDto::getBatchDetailsId))
                );

        //use the batched completed sentence item dtos, to group by user
        Map<Long, List<CompletedSentenceItemDto>> batchDetailSentencesMap = batchSentencesFuture.get(30, TimeUnit.SECONDS);

        Map<String, List<ByteArrayResource>> batchDetailsResources = new HashMap<>();

        for (Map.Entry<Long, List<CompletedSentenceItemDto>> entry : batchDetailSentencesMap.entrySet()) {
            BatchDetailsEntity batchDetailsEntity = batchDetailsEntityMap.get(entry.getKey());
            String language = batchDetailsEntity.getLanguage().getName();

            List<CompletedSentenceItemDto> sentences = entry.getValue();
            //we can generate Excel at this point
            byte[] excelByteArray = generateExcel(sentences);
            String excelName = "Batch_" + entry.getKey() + "_" + language + "_sentences.xlsx";

            if (excelOnly) {
                ByteArrayResource excelResource = new ByteArrayResource(excelByteArray);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + excelName)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .contentLength(excelResource.contentLength())
                        .body(excelResource);
            }

            List<UserDetailDTO> recordedByDetails = sentences.stream()
                    .flatMap(dto -> dto.getAudioDetails().stream())
                    .distinct()
                    .collect(Collectors.toList());

            Map<String, List<UserDetailDTO>> userVoiceMap = recordedByDetails.stream().collect(Collectors.groupingBy(UserDetailDTO::getUsername));

            try {

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos);
                zos.setLevel(Deflater.BEST_SPEED);

                // Synchronization object for thread-safe ZIP writing
                Object zipLock = new Object();

                // Add Excel file to the root of ZIP
                synchronized (zipLock) {
                    ZipEntry excelEntry = new ZipEntry(excelName);
                    zos.putNextEntry(excelEntry);
                    zos.write(excelByteArray);
                    zos.closeEntry();
                }

                // Process downloads in parallel
                List<CompletableFuture<Void>> downloadFutures = new ArrayList<>();
                AtomicInteger totalProcessed = new AtomicInteger(0);

                for (Map.Entry<String, List<UserDetailDTO>> voiceEntry : userVoiceMap.entrySet()) {
                    String username = voiceEntry.getKey();
                    List<UserDetailDTO> userAudios = voiceEntry.getValue();

                    log.info("USER {} HAS {} audios", username, userAudios.size());

                    for (UserDetailDTO audio : userAudios) {
                        CompletableFuture<Void> downloadFuture = CompletableFuture.runAsync(() -> {
                            try {
                                // Download and add to ZIP
                                downloadAndAddToZip(username, audio, zos, zipLock, totalProcessed);
                            } catch (Exception e) {
                                logger.error("Error processing audio for user {}: {}", username, e.getMessage());
                                //throw new CompletionException(e);
                            }
                        }, downloadExecutor);

                        downloadFutures.add(downloadFuture);
                    }
                }

                // Wait for all downloads to complete with timeout
                CompletableFuture.allOf(downloadFutures.toArray(new CompletableFuture[0]))
                        .get(5, TimeUnit.MINUTES);

                synchronized (zipLock) {
                    zos.close();
                }

                ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());
                // we can have the name as the key
                String key = "Batch_" + entry.getKey() + "_" + language + ".zip";
                batchDetailsResources.computeIfAbsent(key, k -> new ArrayList<>()).add(resource);

            } catch (TimeoutException e) {
                throw new GeneralException("Download operation timed out", e);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new GeneralException("Failed to create zip file", e);
            } finally {
                // Don't shut down the executor as it's a shared resource
            }
        }
        if (batchDetailsResources.isEmpty()) {
            throw new BadRequestException("Could not generate zip files");
        }

        //if batch is 1, return the zip as is
        if (batchDetailsResources.size() == 1) {
            Map.Entry<String, List<ByteArrayResource>> entry = batchDetailsResources.entrySet().iterator().next();
            ByteArrayResource resource = entry.getValue().get(0);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + entry.getKey())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);
        } else {

            // If multiple batches, create a parent zip
            try (ByteArrayOutputStream finalBaos = new ByteArrayOutputStream();
                 ZipOutputStream finalZos = new ZipOutputStream(finalBaos)) {

                finalZos.setLevel(Deflater.BEST_SPEED);

                // Add each batch zip to the parent zip
                for (Map.Entry<String, List<ByteArrayResource>> entry : batchDetailsResources.entrySet()) {
                    String batchFileName = entry.getKey();
                    List<ByteArrayResource> resources = entry.getValue();

                    for (ByteArrayResource resource : resources) {
                        ZipEntry zipEntry = new ZipEntry(batchFileName);
                        finalZos.putNextEntry(zipEntry);
                        finalZos.write(resource.getByteArray());
                        finalZos.closeEntry();
                    }
                }

                finalZos.finish();

                ByteArrayResource finalResource = new ByteArrayResource(finalBaos.toByteArray());
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=" + batchDetailsIds.size() + "_batches_" + timestamp + ".zip")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .contentLength(finalResource.contentLength())
                        .body(finalResource);
            } catch (IOException e) {
                throw new GeneralException("Failed to create zip file", e);
            }
        }
    }

    private void downloadAndAddToZip(String username, UserDetailDTO audio, ZipOutputStream zos,
                                     Object zipLock, AtomicInteger counter) throws IOException {
        URL url = new URL(audio.getFileUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(30000);

        String filename = url.getPath().substring(url.getPath().lastIndexOf('/') + 1);

        // Use buffered streams for better performance
        try (BufferedInputStream bis = new BufferedInputStream(connection.getInputStream())) {
            byte[] buffer = new byte[8192];
            ByteArrayOutputStream audioData = new ByteArrayOutputStream();

            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                audioData.write(buffer, 0, bytesRead);
            }

            // Synchronized block for thread-safe ZIP writing
            synchronized (zipLock) {
                ZipEntry zipEntry = new ZipEntry(username + "/" + filename);
                zos.putNextEntry(zipEntry);
                audioData.writeTo(zos);
                zos.closeEntry();

                int processed = counter.incrementAndGet();
                log.info("Processed {}: {}", processed, filename);
            }
        }
    }

    public byte[] generateExcel(List<CompletedSentenceItemDto> batchDetailsList) throws IOException {
        if (batchDetailsList == null || batchDetailsList.isEmpty()) {
            throw new IllegalArgumentException("BatchDetails list cannot be null or empty");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sentence Translations");

            // Find the maximum number of audio details across all sentences
            int maxAudioDetails = batchDetailsList.stream()
                    .mapToInt(details -> details.getAudioDetails().size())
                    .max()
                    .orElse(0);

            // Create dynamic headers based on max audio details
            String[] baseHeaders = {"#", "Sentence", "Translated Sentence", "No of Audios"};
            String[] headers = new String[baseHeaders.length + (maxAudioDetails * 2)];
            System.arraycopy(baseHeaders, 0, headers, 0, baseHeaders.length);

            // Add dynamic audio headers
            for (int i = 0; i < maxAudioDetails; i++) {
                headers[baseHeaders.length + (i * 2)] = "Audio-" + (i + 1) + "-Url";
                headers[baseHeaders.length + (i * 2) + 1] = "Audio-" + (i + 1) + "-Recorded-By";
            }

            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create header cells
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            AtomicInteger rowNum = new AtomicInteger(1);
            batchDetailsList.forEach(batchDetails -> {
                Row row = sheet.createRow(rowNum.getAndIncrement());

                // Serial number
                row.createCell(0).setCellValue(rowNum.get() - 1);

                // Sentence
                row.createCell(1).setCellValue(batchDetails.getSentenceText());

                // Translated sentence
                row.createCell(2).setCellValue(batchDetails.getTranslatedText());

                // Handle audio details dynamically
                List<UserDetailDTO> audioDetails = batchDetails.getAudioDetails();

                //add audio count
                row.createCell(3).setCellValue(audioDetails.size());

                for (int i = 0; i < audioDetails.size(); i++) {
                    UserDetailDTO audio = audioDetails.get(i);
                    // Audio URL cell
                    row.createCell(baseHeaders.length + (i * 2))
                            .setCellValue(audio.getFileUrl());
                    // Username cell
                    row.createCell(baseHeaders.length + (i * 2) + 1)
                            .setCellValue(audio.getUsername());
                }
            });

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new IOException("Failed to generate Excel file", e);
        }
    }

    public List<UserDetailDTO> getVoiceDetailsByTranslatedSentenceId(List<Long> translatedSentenceIds) {
        log.info("Entering getVoiceDetailsByTranslatedSentenceId with translatedSentenceIds: {}", translatedSentenceIds);

        List<Object[]> result = translatedSentenceRepository.fetchVoiceDetailsByTranslatedSentenceId(translatedSentenceIds);

        if (result == null || result.isEmpty()) {
            log.info("No voice details found for translatedSentenceId: {}", translatedSentenceIds);
            return Collections.emptyList();
        }

        List<UserDetailDTO> userDetailDtos = new ArrayList<>();
        for (Object[] row : result) {
            if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                log.info("Invalid data in voice details for translatedSentenceId: {}", translatedSentenceIds);
                return Collections.emptyList();
            }

            String fileUrl = (String) row[0];
            Long userId = ((Number) row[1]).longValue();
            String username = (String) row[2];
            Long translatedSentenceId = ((Number) row[3]).longValue();

            log.info("fileUrl: {} -- userID: {} -- Username:{}", fileUrl, userId, username);

            UserDetailDTO userDetailDto = new UserDetailDTO();
            userDetailDto.setUserId(userId);
            userDetailDto.setUsername(username);
            userDetailDto.setFileUrl(fileUrl);
            userDetailDto.setTranslatedSentenceId(translatedSentenceId);
            userDetailDtos.add(userDetailDto);
        }


        return userDetailDtos;
    }

    private List<TranslatedSentenceItemDto> getTranslatedSentenceItemDtos(List<TranslatedSentenceEntity> translatedSentences, Boolean isFirstReviewed, Boolean isExpertReviewed) {
        List<TranslatedSentenceItemDto> unreviewedTranslatedSentencesDto = translatedSentences.stream().map(translatedSentence -> {
            Boolean isAccepted = null;
            if (isFirstReviewed != null && isFirstReviewed.booleanValue()) {
                isAccepted = translatedSentence.getReviewStatus().ordinal() == 0;
            } else if (isExpertReviewed != null && isExpertReviewed.booleanValue()) {
                isAccepted = translatedSentence.getSecondReview().ordinal() == 0;
            }
            ModeratorCommentEntity moderatorCommentEntity = this.moderatorCommentRepo.findAllByTranslatedSentence_TranslatedSentenceId(translatedSentence.getTranslatedSentenceId());
            String moderatorComment = "";
            if (moderatorCommentEntity != null)
                moderatorComment = moderatorCommentEntity.getComment();
            if (translatedSentence.getBatchDetails().getBatch().getBatchType() == BatchType.AUDIO)
                translatedSentence.getSentence().setAudioLink(this.amazonClient.generatePresignedUrl(translatedSentence.getSentence().getAudioLink()));
            return TranslatedSentenceItemDto.entityToDto(translatedSentence, moderatorComment, isAccepted);
        }).collect(Collectors.toList());
        return unreviewedTranslatedSentencesDto;
    }

    @Transactional
    public ResponseEntity<ResponseMessage> markTranslationAsComplete(Long batchDetailsId) {
        HashMap<String, Object> result = getBatchDetails(batchDetailsId);
        ResponseEntity<ResponseMessage> response = (ResponseEntity<ResponseMessage>) result.get("response");
        if (response != null)
            return response;
        BatchDetailsEntity batchDetails = (BatchDetailsEntity) result.get("batchDetails");
        if (batchDetails == null)
            return ResponseEntity.internalServerError().body(new ResponseMessage("An error occured"));
        if (batchDetails.getBatchStatus().ordinal() < BatchStatus.TRANSLATED.ordinal()) {
            batchDetails.setBatchStatus(BatchStatus.TRANSLATED);
            this.batchDetailsRepo.save(batchDetails);
        }
        return ResponseEntity.ok(new ResponseMessage("Translations marked as complete"));
    }

    @Transactional
    public ResponseEntity<ResponseMessage> markModerationAsComplete(Long batchDetailsId) {
        HashMap<String, Object> result = getBatchDetails(batchDetailsId);
        ResponseEntity<ResponseMessage> response = (ResponseEntity<ResponseMessage>) result.get("response");
        if (response != null)
            return response;
        BatchDetailsEntity batchDetails = (BatchDetailsEntity) result.get("batchDetails");
        if (batchDetails == null)
            return ResponseEntity.internalServerError().body(new ResponseMessage("An error occurred"));
        if (batchDetails.getBatchStatus().ordinal() < BatchStatus.TRANSLATION_VERIFIED.ordinal()) {
            Optional<BatchDetailsStatsEntity> optionalTranslatorStats = this.batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetailsId);
            if (optionalTranslatorStats.isPresent()) {
                Integer rejectedSentences = this.translatedSentenceRepo.countAllByBatchDetailsIdAndReviewStatus(batchDetailsId, StatusTypes.REJECTED);
                if (rejectedSentences <= 0) {
                    batchDetails.setBatchStatus(BatchStatus.TRANSLATION_VERIFIED);
                    this.batchDetailsRepo.save(batchDetails);
                }
            }
        }
        return ResponseEntity.ok(new ResponseMessage("Translations marked as verified"));
    }

    public ResponseEntity getAffectedBatcheDetails() {
        List<Long> batchDetailsIds = this.batchDetailsRepo.findAllBatchDetailsId();
        ArrayList<Long> malformedBatchDetailsIds = new ArrayList<>();
        for (Long batchDetailsId : batchDetailsIds) {
            HashMap<String, Object> result = getBatchDetails(batchDetailsId);
            ResponseEntity<ResponseMessage> response = (ResponseEntity<ResponseMessage>) result.get("response");
            if (response != null)
                return response;
            BatchDetailsEntity batchDetails = (BatchDetailsEntity) result.get("batchDetails");
            if (batchDetails == null)
                return ResponseEntity.internalServerError().body(new ResponseMessage("An error occurred"));
            if (batchDetails.getBatchStatus().ordinal() < BatchStatus.TRANSLATION_VERIFIED.ordinal() && batchDetails.getBatchStatus().ordinal() >= BatchStatus.TRANSLATED.ordinal()) {
                Optional<BatchDetailsStatsEntity> optionalTranslatorStats = this.batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetailsId);
                if (optionalTranslatorStats.isPresent()) {
                    Integer approvedSentences = this.translatedSentenceRepo.countAllByBatchDetailsIdAndReviewStatus(batchDetailsId, StatusTypes.APPROVED);
                    Integer totalSentences = this.translatedSentenceRepo.countAllByBatchDetailsBatchDetailsId(batchDetailsId);
                    if (approvedSentences >= totalSentences)
                        malformedBatchDetailsIds.add(batchDetails.getBatchDetailsId());
                }
            }
        }
        return ResponseEntity.ok(malformedBatchDetailsIds);
    }

    @Transactional
    public ResponseEntity<ResponseMessage> markExpertVerificationAsComplete(Long batchDetailsId) {
        HashMap<String, Object> result = getBatchDetails(batchDetailsId);
        ResponseEntity<ResponseMessage> response = (ResponseEntity<ResponseMessage>) result.get("response");
        if (response != null)
            return response;
        BatchDetailsEntity batchDetails = (BatchDetailsEntity) result.get("batchDetails");
        if (batchDetails == null)
            return ResponseEntity.internalServerError().body(new ResponseMessage("An error occurred"));
        if (batchDetails.getBatchStatus().ordinal() < BatchStatus.SECOND_VERIFICATION_DONE.ordinal()) {
            Optional<BatchDetailsStatsEntity> optionalUsersStats = this.batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetailsId);
            if (optionalUsersStats.isPresent()) {
                Integer rejectedSentences = this.translatedSentenceRepo.countAllByBatchDetailsIdAndSecondReview(batchDetailsId, StatusTypes.REJECTED);
                if (rejectedSentences <= 0) {
                    batchDetails.setBatchStatus(BatchStatus.SECOND_VERIFICATION_DONE);
                    this.batchDetailsRepo.save(batchDetails);
                }
            }
        }
        return ResponseEntity.ok(new ResponseMessage("Translations marked as expert verified"));
    }

    @Transactional
    public ResponseEntity<ResponseMessage> markBatchAsRecorded(Long batchDetailsId) {
        HashMap<String, Object> result = getBatchDetails(batchDetailsId);
        ResponseEntity<ResponseMessage> response = (ResponseEntity<ResponseMessage>) result.get("response");
        if (response != null)
            return response;
        BatchDetailsEntity batchDetails = (BatchDetailsEntity) result.get("batchDetails");
        if (batchDetails == null)
            return ResponseEntity.internalServerError().body(new ResponseMessage("An error occurred"));

        //check voice count ensure all recorders have recorded
        List<Long> recorderIds = batchDetailsRepo.fetchAssignedUserIdsFromBatchDetails(batchDetailsId, UserBatchRole.AUDIO_RECORDER);
        if (recorderIds == null || recorderIds.isEmpty()) {
            log.info("NO RECORDERS FOUND..........");
        } else {
            List<Long> voiceIds = voiceRepo.fetchVoiceIdsByRecorderIds(batchDetailsId, recorderIds);
            if (voiceIds == null || voiceIds.isEmpty()) {
                log.info("NO VOICES FOUND.............");
            } else {
                if (recorderIds.size() != voiceIds.size()) {
                    return ResponseEntity.ok(new ResponseMessage("All voice recorders have not recorded"));
                }
            }
        }

        if (batchDetails.getBatchStatus().ordinal() < BatchStatus.RECORDED.ordinal()) {
            List<TranslatedSentenceEntity> voiceTasks = this.voiceService.findVoiceTasks(batchDetailsId);
            if (voiceTasks.isEmpty()) {
                batchDetails.setBatchStatus(BatchStatus.RECORDED);
                this.batchDetailsRepo.save(batchDetails);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.ok(new ResponseMessage("Translations marked as recorded"));
    }

    @Transactional
    public ResponseMessage markAudioReviewAsComplete(Long batchDetailsId, boolean expertReview) {

        BatchDetailsEntity batchDetails = batchDetailsRepo.findById(batchDetailsId).orElse(null);
        if (batchDetails == null)
            throw new NotFoundException("Batch details not found");

        if (!expertReview) {
            if (batchDetails.getBatchStatus().ordinal() < BatchStatus.AUDIO_VERIFIED.ordinal()) {
                batchDetails.setBatchStatus(BatchStatus.AUDIO_VERIFIED);
            }
        } else {
            if (batchDetails.getBatchStatus().ordinal() < BatchStatus.EXPERT_AUDIO_VERIFIED.ordinal()) {
                batchDetails.setBatchStatus(BatchStatus.EXPERT_AUDIO_VERIFIED);
            }
        }

        batchDetailsRepo.save(batchDetails);
        return new ResponseMessage("Audios marked as verified");
    }

    public HashMap<String, Object> getBatchDetails(Long batchDetailsId) {
        HashMap<String, Object> result = new HashMap<>();
        if (batchDetailsId == null) {
            result.put("response", ResponseEntity.badRequest().body(new ResponseMessage("Please provide batch details id")));
            return result;
        }
        Optional<BatchDetailsEntity> optionalBatchDetails = this.batchDetailsRepo.findById(batchDetailsId);
        if (optionalBatchDetails.isEmpty()) {
            result.put("response", ResponseEntity.badRequest().body(new ResponseMessage("Batch details not found")));
        } else {
            result.put("batchDetails", optionalBatchDetails.get());
        }
        return result;
    }

    @Transactional
    public ResponseMessage deleteBatchDetails(Long batchDetailsId) {
        if (batchDetailsId == null)
            throw new BadRequestException("Batch details id is required");
        if (this.batchDetailsRepo.findById(batchDetailsId).isEmpty())
            throw new BadRequestException("Batch details does not exist");
        this.translatedSentenceRepo.deleteAllByBatchDetailsBatchDetailsId(batchDetailsId);
        this.batchDetailsStatsRepository.deleteAllByBatchDetailsBatchDetailsId(batchDetailsId);
        this.batchDetailsRepo.deleteById(batchDetailsId);
        return new ResponseMessage("Batch details successfully deleted");
    }

    public ResponseEntity<Object> getExpertReviewedSentences(Long languageId) {
        if (languageId == null)
            return ResponseEntity.badRequest().body(new ResponseMessage("Language Id is required"));
        Optional<Language> language = this.languageRepository.findById(languageId);
        if (language.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessage("Language Id not found"));
        List<CompletedSentenceItemDto> expertReviewedSentences = getSentencesWithPresignedAudioUrl(this.batchDetailsRepo.getAllSentencesInLanguagePerBatchDetailsStatus(languageId, BatchStatus.SECOND_VERIFICATION_DONE.ordinal()));
        return ResponseEntity.ok().body(new ExpertReviewedSentencesDto(language
                .get().getName(), expertReviewedSentences));
    }

    public List<CompletedSentenceItemDto> getSentencesWithPresignedAudioUrl(List<SentenceItemDto> sentences) {
        return sentences.stream().map(sentenceItemDto -> {
            log.info("Processing sentenceItem with translatedSentenceId: {}", sentenceItemDto.getTranslatedSentenceId());

            // Fetch the recordedBy information
            List<UserDetailDTO> recordedBy = getVoiceDetailsByTranslatedSentenceId(List.of(sentenceItemDto.getTranslatedSentenceId()));
            if (recordedBy == null) {
                log.info("recordedBy is null for translatedSentenceId: {}", sentenceItemDto.getTranslatedSentenceId());
            }

            UserDetailDTO userDetailDTO = recordedBy != null && !recordedBy.isEmpty() ? recordedBy.get(0) : null;

            // Create the CompletedSentenceItemDto
            CompletedSentenceItemDto completedSentence = new CompletedSentenceItemDto(sentenceItemDto, userDetailDTO);

            // Generate presigned URLs for audio files
            if (completedSentence.getAudioUrl() != null) {
                String presignedUrl = amazonClient.generatePresignedUrl(completedSentence.getAudioUrl());
                completedSentence.setAudioUrl(presignedUrl);
            }
            if (completedSentence.getTranscriptionAudioUrl() != null) {
                String presignedUrl = amazonClient.generatePresignedUrl(completedSentence.getTranscriptionAudioUrl());
                completedSentence.setTranscriptionAudioUrl(presignedUrl);
            }
            return completedSentence;
        }).collect(Collectors.toList());
    }


    private List<BatchInfoItemDTO> getSortedTranslationBatchDetails(List<BatchDetailsEntity> batchDetails) {
        return batchDetails.stream()
                .map(element -> {
                    Integer rejectedSentences = this.translatedSentenceRepo.countRejectedSentences(element.getBatchDetailsId(), StatusTypes.REJECTED, StatusTypes.REJECTED);
                    BatchInfoItemDTO batchInfoItemDTO = new BatchInfoItemDTO(element);
                    if (batchInfoItemDTO.getTranslated()) {
                        batchInfoItemDTO.setPendingSentences(rejectedSentences);
                        batchInfoItemDTO.setTranslated(rejectedSentences <= 0);
                    }
                    return batchInfoItemDTO;
                }).sorted((e1, e2) ->
                        (!e1.getTranslated() && e2.getTranslated()) ? -1 : (
                                (e1.getTranslated() && !e2.getTranslated()) ? 1 : 0))

                .collect(Collectors.toList());
    }

    private List<BatchInfoItemDTO> getSortedReviewerBatchDetails(List<BatchDetailsEntity> translationBatchDetails) {
        return translationBatchDetails.stream()
                .map(element -> {
                    Integer unreviewedSentences = this.translatedSentenceRepo.countAllByBatchDetailsIdAndReviewStatus(element.getBatchDetailsId(), StatusTypes.UNREVIEWED);
                    BatchInfoItemDTO batchInfoItemDTO = new BatchInfoItemDTO(element);
                    batchInfoItemDTO.setReviewed(unreviewedSentences <= 0);
                    batchInfoItemDTO.setPendingSentences(unreviewedSentences);
                    return batchInfoItemDTO;
                }).sorted((e1, e2) ->
                        (!e1.getReviewed() && e2.getReviewed()) ? -1 : (
                                (e1.getReviewed() && !e2.getReviewed()) ? 1 : 0))

                .collect(Collectors.toList());
    }

    private List<BatchInfoItemDTO> getSortedExpertReviewerBatchDetails(List<BatchDetailsEntity> translationBatchDetails) {
        return translationBatchDetails.stream()
                .map(element -> {
                    Integer unreviewedSentences = this.translatedSentenceRepo.countAllByBatchDetailsIdAndSecondReview(element.getBatchDetailsId(), StatusTypes.UNREVIEWED);
                    BatchInfoItemDTO batchInfoItemDTO = new BatchInfoItemDTO(element);
                    batchInfoItemDTO.setExpertReviewed(unreviewedSentences <= 0);
                    batchInfoItemDTO.setPendingSentences(unreviewedSentences);
                    return batchInfoItemDTO;
                }).sorted((e1, e2) ->
                        (!e1.getExpertReviewed() && e2.getExpertReviewed()) ? -1 : (
                                (e1.getExpertReviewed() && !e2.getExpertReviewed()) ? 1 : 0))

                .collect(Collectors.toList());
    }

    public boolean createBatchFromFeedback(FeedbackDTO feedbackDTO) {
        Language targetLanguage = languageRepository.findByName(feedbackDTO.getTargetLanguage());
        if (targetLanguage == null)
            throw new BadRequestException(String.format("Target Language (%s) not found", feedbackDTO.getTargetLanguage()));

        Language sourceLanguage = languageRepository.findByName(feedbackDTO.getSourceLanguage());
        if (sourceLanguage == null)
            throw new BadRequestException(String.format("Source Language (%s) not found", feedbackDTO.getSourceLanguage()));

        BatchEntity batch = batchRepo.findByTargetLanguage_LanguageIdAndSourceLanguage_LanguageIdAndFromFeedback(targetLanguage.getLanguageId(), sourceLanguage.getLanguageId(), YesNo.YES);
        BatchDetailsEntity batchDetailsEntity = null;
        if (batch != null)
            batchDetailsEntity = batchDetailsRepo.findByBatchIdAndLanguageId(batch.getBatchNo(), targetLanguage.getLanguageId());

        BatchDetailsStatsEntity batchDetailsStatsEntity = null;
        if (batchDetailsEntity != null)
            batchDetailsStatsEntity = batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetailsEntity.getBatchDetailsId()).orElse(null);

        if (batch == null) {
            batch = new BatchEntity();
            batch.setSource("User feedback");
            batch.setBatchType(BatchType.TEXT);
            batch.setFromFeedback(YesNo.YES);
            batch.setBatchOrigin(BatchOrigin.FEEDBACK);
            batch.setDescription(String.format("User feedback from %s to %s", sourceLanguage.getName(), targetLanguage.getName()));
            batch.setDeletionStatus(DeletionStatus.NOT_DELETED);
            batch.setTargetLanguage(targetLanguage);
            batch.setSourceLanguage(sourceLanguage);
            batch = batchRepo.save(batch);
        }

        if (batchDetailsEntity == null) {
            batchDetailsEntity = new BatchDetailsEntity();
            batchDetailsEntity.setBatchId(batch.getBatchNo());
            batchDetailsEntity.setBatchStatus(BatchStatus.TRANSLATED);
            batchDetailsEntity.setLanguage(targetLanguage);
            batchDetailsEntity.setDeletionStatus(DeletionStatus.NOT_DELETED);
            batchDetailsEntity = batchDetailsRepo.save(batchDetailsEntity);
        }

        Sentence sentence = new Sentence();
        sentence.setBatchNo(batch.getBatchNo());
        sentence.setSentenceText(feedbackDTO.getSourceText());
        sentence.setDateCreated(new Date());
        sentence = sentenceRepository.save(sentence);

        if (batchDetailsStatsEntity == null) {
            batchDetailsStatsEntity = new BatchDetailsStatsEntity();
            batchDetailsStatsEntity.setBatchDetails(batchDetailsEntity);
            batchDetailsStatsEntity.setSentencesTranslated(1);
            batchDetailsStatsEntity.setDeletionStatus(DeletionStatus.NOT_DELETED);
            batchDetailsStatsRepository.save(batchDetailsStatsEntity);
        } else {
            batchDetailsStatsEntity.setSentencesTranslated(batchDetailsStatsEntity.getSentencesTranslated() + 1);
            batchDetailsStatsRepository.save(batchDetailsStatsEntity);
        }

        TranslatedSentenceEntity translatedSentenceEntity = new TranslatedSentenceEntity();
        translatedSentenceEntity.setDateCreated(new Date());
        translatedSentenceEntity.setReviewStatus(StatusTypes.UNREVIEWED);
        translatedSentenceEntity.setSentenceId(sentence.getSentenceId());
        translatedSentenceEntity.setTranslatedText(feedbackDTO.getEditedTranslatedText());
        translatedSentenceEntity.setBatchDetailsId(batchDetailsEntity.getBatchDetailsId());
        translatedSentenceEntity.setLanguage(targetLanguage);
        translatedSentenceEntity.setDeletionStatus(DeletionStatus.NOT_DELETED);
        translatedSentenceRepository.save(translatedSentenceEntity);

        return true;
    }

    @Transactional
    public ResponseEntity<?> assignUserRoleToBatch(Long batchDetailsId, UserBatchRole role, BatchUserAssignmentDTO assignmentDTO) {
        Optional<BatchDetailsEntity> optionalBatchDetails = this.batchDetailsRepo.findById(batchDetailsId);
        if (optionalBatchDetails.isEmpty())
            return ResponseEntity.badRequest().body(new ResponseMessage("Batch details does not exist"));

        BatchDetailsEntity batchDetailsEntity = optionalBatchDetails.get();
        if (batchDetailsEntity.getBatch().getBatchType() == BatchType.AUDIO)
            return ResponseEntity.badRequest().body(new ResponseMessage("Audio batches cannot be assigned a recorder"));

        if (assignmentDTO.getUserIds().isEmpty() && assignmentDTO.getUserId() != null)
            assignmentDTO.setUserIds(List.of(assignmentDTO.getUserId()));

        //boolean useLegacyImplementation = !role.equals(UserBatchRole.AUDIO_RECORDER) && !role.equals(UserBatchRole.EXPERT_AUDIO_REVIEWER);

        //delete existing assignments
        batchDetailsUserAssigmentRepo.deleteAllByBatchDetailsIdAndBatchRoleAndNotInUserIds(batchDetailsId, role, assignmentDTO.getUserIds());

        for (Long userId : assignmentDTO.getUserIds()) {
            List<BatchDetailsUserAssignment> userAssignments =
                    batchDetailsUserAssigmentRepo.findByUserIdAndBatchRoleAndBatchDetails_BatchDetailsId(userId, role, batchDetailsId);
            if (userAssignments.isEmpty())
                createBatchUserAssignment(userId, batchDetailsEntity, batchDetailsEntity.getBatchId(), role);

//            if (useLegacyImplementation) {
            switch (role) {
                case TEXT_TRANSLATOR:
                    batchDetailsEntity.setTranslatedById(userId);
                    batchDetailsEntity.setBatchStatus(BatchStatus.ASSIGNED_TRANSLATOR);
                    break;
                case TEXT_VERIFIER:
                    batchDetailsEntity.setTranslationVerifiedById(userId);
                    batchDetailsEntity.setBatchStatus(BatchStatus.ASSIGNED_TEXT_VERIFIER);
                    break;
                case EXPERT_TEXT_REVIEWER:
                    batchDetailsEntity.setSecondReviewerId(userId);
                    batchDetailsEntity.setBatchStatus(BatchStatus.ASSIGNED_RECORDER);

                    int noOfSentencesToReview = (int) Math.ceil(0.1D * batchDetailsEntity.getTranslatedSentence().size());
                    List<TranslatedSentenceEntity> sentencesToReview = batchDetailsEntity.getTranslatedSentence().subList(0, noOfSentencesToReview);
                    List<Long> translatedSentencesToReviewIds = sentencesToReview.stream().map(TranslatedSentenceEntity::getTranslatedSentenceId)
                            .collect(Collectors.toList());
                    this.translatedSentenceRepo.assignSentencesToExpertReviewer(translatedSentencesToReviewIds);
                    break;
                case AUDIO_VERIFIER:
                    batchDetailsEntity.setAudioVerifiedById(userId);
                    batchDetailsEntity.setBatchStatus(BatchStatus.ASSIGNED_AUDIO_VERIFIER);
                    break;
                case EXPERT_AUDIO_REVIEWER:
                    batchDetailsEntity.setBatchStatus(BatchStatus.ASSIGNED_EXPERT_AUDIO_REVIEWER);
                    break;
                case AUDIO_RECORDER:
                    batchDetailsEntity.setBatchStatus(BatchStatus.ASSIGNED_RECORDER);
                    break;
                default:
                    log.info("NO ROLE....");
            }
            batchDetailsRepo.save(batchDetailsEntity);

        }

        return ResponseEntity.ok(true);
    }

    public ResponseMessage moveBatchAssignmentsFromLegacyTable() {
        List<BatchDetailsEntity> batchDetailsEntities = batchDetailsRepo.findAll();
        for (BatchDetailsEntity batchDetailsEntity : batchDetailsEntities) {
            Long batchId = batchDetailsEntity.getBatchId();

            if (batchDetailsEntity.getTranslatedById() != null)
                createBatchUserAssignment(batchDetailsEntity.getTranslatedById(), batchDetailsEntity, batchId, UserBatchRole.TEXT_TRANSLATOR);

            if (batchDetailsEntity.getTranslationVerifiedById() != null)
                createBatchUserAssignment(batchDetailsEntity.getTranslationVerifiedById(), batchDetailsEntity, batchId, UserBatchRole.TEXT_VERIFIER);

            if (batchDetailsEntity.getSecondReviewerId() != null)
                createBatchUserAssignment(batchDetailsEntity.getSecondReviewerId(), batchDetailsEntity, batchId, UserBatchRole.EXPERT_TEXT_REVIEWER);

            if (batchDetailsEntity.getRecordedById() != null)
                createBatchUserAssignment(batchDetailsEntity.getRecordedById(), batchDetailsEntity, batchId, UserBatchRole.AUDIO_RECORDER);

            if (batchDetailsEntity.getAudioVerifiedById() != null)
                createBatchUserAssignment(batchDetailsEntity.getAudioVerifiedById(), batchDetailsEntity, batchId, UserBatchRole.AUDIO_VERIFIER);

        }
        return new ResponseMessage("Successfully moved legacy table");
    }

    private void createBatchUserAssignment(Long userId, BatchDetailsEntity batchDetails, Long batchId, UserBatchRole role) {
        BatchDetailsUserAssignment batchDetailsUserAssignment = new BatchDetailsUserAssignment();
        batchDetailsUserAssignment.setUserId(userId);
        batchDetailsUserAssignment.setBatchDetailsId(batchDetails.getBatchDetailsId());
        batchDetailsUserAssignment.setBatchId(batchId);
        batchDetailsUserAssignment.setBatchRole(role);

        if (role == UserBatchRole.EXPERT_AUDIO_REVIEWER) {
            //get 10 % of audios
            List<VoiceEntity> voices = voiceRepo.findAllByBatchDetailsIdAndStatus(batchDetails.getBatchDetailsId(), StatusTypes.APPROVED);
            if (voices.isEmpty())
                throw new NotFoundException("No audio Recordings found for this batch");

            int noOfAudiosToReview = (int) Math.ceil(0.1D * voices.size());
            List<VoiceEntity> voicesToReview = voices.subList(0, noOfAudiosToReview);

            voiceRepo.assignExpertReviewer(voicesToReview.stream().map(VoiceEntity::getVoiceId).collect(Collectors.toList()), userId);

        }
        batchDetailsUserAssigmentRepo.save(batchDetailsUserAssignment);
    }

    public ResponseEntity<byte[]> getCompletedSentences(Long batchDetailsId) {
        // Assuming this list is obtained from your repository
        List<SentenceItemDto> sentences = batchDetailsRepo.getAllSentencesInBatchDetails(Collections.singletonList(batchDetailsId));

        if (sentences.isEmpty()) {
            String errorMessage = "No completed sentences found for batchDetailsId: " + batchDetailsId;
            return ResponseEntity.badRequest().body(errorMessage.getBytes());
        }

        // Create a map to store the unique sentences based on their IDs
        Map<Long, SentenceItemDto> uniqueSentences = new HashMap<>();
        for (SentenceItemDto sentence : sentences) {
            // Check if the sentence already exists in the map
            if (!uniqueSentences.containsKey(sentence.getSentenceId())) {
                // If not, add it to the map
                uniqueSentences.put(sentence.getSentenceId(), sentence);
            }
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (SentenceItemDto sentence : uniqueSentences.values()) {
                    String audioUrl = sentence.getAudioUrl();
                    if (audioUrl == null || audioUrl.isEmpty()) {
                        // Skip processing for records with null or empty audio URL
                        continue;
                    }

                    try {
                        // Create a valid URL object
                        URL url = new URL(audioUrl);

                        // Extract filename from URL
                        String[] pathSegments = url.getPath().split("/");
                        String filename = pathSegments[pathSegments.length - 1];

                        // Open connection and retrieve input stream
                        try (InputStream inputStream = url.openStream()) {
                            byte[] audioFileBytes = inputStream.readAllBytes();

                            // Add voice file to the ZIP file
                            ZipEntry zipEntry = new ZipEntry(filename);
                            zos.putNextEntry(zipEntry);
                            zos.write(audioFileBytes);
                            zos.closeEntry();
                        }
                    } catch (MalformedURLException e) {
                        // Handle MalformedURLException
                        e.printStackTrace();
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .body(("Malformed URL: " + audioUrl).getBytes());
                    } catch (IOException e) {
                        // Handle IOException (URL not found or other IO errors)
                        e.printStackTrace();
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .body(("Error reading audio file from URL: " + audioUrl).getBytes());
                    }
                }
            }

            byte[] zipBytes = baos.toByteArray();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=batch_" + batchDetailsId + "_audio.zip")
                    .body(zipBytes);
        } catch (IOException e) {
            // Handle IOException (ZIP creation or other IO errors)
            log.error(e.getMessage());
            String errorMessage = "Error creating ZIP file for batchDetailsId: " + batchDetailsId + ". Please try again later.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(errorMessage.getBytes());
        }
    }

    @Transactional
    public ResponseMessage addBatchReReview(Long languageId, List<BatchReviewDTO> reviewDTOS) {
        if (languageId == null)
            throw new BadRequestException("Language is required");

        Language language = languageRepository.findById(languageId).stream().findFirst().orElse(null);
        if (language == null)
            throw new BadRequestException("Language not found");

        // Group the list into batches of maximum 500 sentences
        List<List<BatchReviewDTO>> groupedList = new ArrayList<>();
        int groupSize = 500;

        for (int i = 0; i < reviewDTOS.size(); i += groupSize) {
            int end = Math.min(reviewDTOS.size(), i + groupSize);
            List<BatchReviewDTO> batch = reviewDTOS.subList(i, end);
            groupedList.add(new ArrayList<>(batch));
        }

        log.info("We have {} grouped for review", groupedList.size());

        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        for (List<BatchReviewDTO> batch : groupedList) {

            List<CreateSentenceDTO> sentences = new ArrayList<>();
            for (BatchReviewDTO batchReviewDTO : batch) {
                log.info("datasetSentence ... {}", batchReviewDTO.getDatasetSentence());
                log.info("englishSentence ... {}", batchReviewDTO.getEnglishSentence());
                //we will get the original sentence
                List<Long> translatedSentenceIds = translatedSentenceRepo.findByTranslatedTextAndOriginalSentenceAndLanguageId(batchReviewDTO.getDatasetSentence(),
                        batchReviewDTO.getEnglishSentence(), language.getLanguageId());
                log.info("FOUND {} TRANSLATED SENTENCES...", translatedSentenceIds.size());

                if (!translatedSentenceIds.isEmpty()) {
                    translatedSentenceRepo.updateTranslatedSentencesForReview(translatedSentenceIds);
                  /*  translatedSentenceRepo.
                            TranslatedSentenceEntity translatedSentenceEntity = translatedSentenceEntities.get(0);
                    translatedSentenceEntity.setSentenceStatus(SentenceStatus.MARKED_FOR_RE_REVIEW);
                    translatedSentenceEntity.setDeletionStatus(DeletionStatus.DELETED);
                    translatedSentenceRepo.save(translatedSentenceEntity);*/
                }

                sentences.add(CreateSentenceDTO.builder().sentenceText(batchReviewDTO.getEnglishSentence()).build());
            }

            BatchDTO batchDTO = new BatchDTO();
            batchDTO.setDescription("Batch from Re-Review created on " + currentDate);
            batchDTO.setSource("Re-review batch - " + language.getName());
            batchDTO.setLinkUrl("");
            batchDTO.setSentences(sentences);
            batchDTO.setBatchOrigin(BatchOrigin.RE_REVIEW);
            this.addBatch(batchDTO);
        }

        return new ResponseMessage("Successfully added sentences for re-reviews");
    }

    public Page<BatchResponseDTO> getAllBatches(String batchType, Integer page, Integer pageSize, BatchOrigin batchOrigin) {
        log.info("Received request to get all batches with type: {}", batchType);
        BatchType batchTypeEnum = BatchType.fromName(batchType).orElse(BatchType.TEXT);

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<BatchEntity> batchEntities;
        if (batchTypeEnum == BatchType.TEXT_FEEDBACK)
            batchEntities = batchRepo.findAllByBatchTypeAndFromFeedback(BatchType.TEXT, YesNo.YES, pageable, batchOrigin);
        else
            batchEntities = batchRepo.findAllByBatchType(batchTypeEnum, pageable, batchOrigin);

        if (batchEntities == null)
            return new PageImpl<>(Collections.emptyList());

        List<BatchResponseDTO> batchResponseDTOS = batchEntities
                .stream()
                .map(BatchResponseDTO::new)
                .collect(Collectors.toList());
        return new PageImpl<>(batchResponseDTOS, pageable, batchEntities.getTotalElements());
    }
}

