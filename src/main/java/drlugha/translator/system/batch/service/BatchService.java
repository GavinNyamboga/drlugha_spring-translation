package drlugha.translator.system.batch.service;

import drlugha.translator.configs.AmazonClient;
import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.shared.enums.DeletionStatus;
import drlugha.translator.shared.enums.StatusTypes;
import drlugha.translator.shared.enums.YesNo;
import drlugha.translator.shared.exception.BadRequestException;
import drlugha.translator.system.batch.dto.*;
import drlugha.translator.system.batch.enums.BatchStatus;
import drlugha.translator.system.batch.enums.BatchType;
import drlugha.translator.system.batch.enums.Task;
import drlugha.translator.system.batch.enums.UserBatchRole;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
        batchDetails.setBatchStatus(BatchStatus.assignedTranslator);
        batchDetails = this.batchDetailsRepo.save(batchDetails);

        //create a record on the new table
        createBatchUserAssignment(batchDetailsDto.getTranslatedById(), batchDetails.getBatchDetailsId(), batchEntity.getBatchNo(), UserBatchRole.TEXT_TRANSLATOR);

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
        batchDetails1.setBatchStatus(BatchStatus.assignedTextVerifier);
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
        List<Long> translatedSentencesToReviewIds = sentencesToReview.stream().map(TranslatedSentenceEntity::getTranslatedSentenceId).collect(Collectors.toList());
        this.translatedSentenceRepo.assignSentencesToExpertReviewer(translatedSentencesToReviewIds);
        if (Objects.nonNull(batchDetails.getSecondReviewerId()))
            batchDetails1.setSecondReviewerId(batchDetails.getSecondReviewerId());
        batchDetails1.setBatchStatus(BatchStatus.assignedExpertReviewer);
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
        batchDetailsEntity.setBatchStatus(BatchStatus.assignedRecorder);
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
        batchDetailsEntity.setBatchStatus(BatchStatus.assignedAudioVerifier);
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
        List<BatchDetailsEntity> batchDetails = this.batchDetailsRepo.findAllByRecordedById(userId);
        List<BatchInfoItemDTO> sortedBatchDetails = batchDetails.stream().map(element -> {
            Integer rejectedAudios = this.voiceRepo.countAllByStatusAndTranslatedSentenceBatchDetailsId(StatusTypes.rejected, element.getBatchDetailsId());
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
        List<BatchDetailsEntity> batchDetails = this.batchDetailsRepo.findAllByAudioVerifiedById(userId);
        List<BatchInfoItemDTO> sortedBatchDetails = batchDetails.stream().map(element -> {
            Integer unreviewedAudios = this.voiceRepo.countAllByStatusAndTranslatedSentenceBatchDetailsId(StatusTypes.unreviewed, element.getBatchDetailsId());
            BatchInfoItemDTO batchInfoItemDTO = new BatchInfoItemDTO(element);
            if (batchInfoItemDTO.getAudioReviewed())
                batchInfoItemDTO.setAudioRecorded(unreviewedAudios <= 0);
            batchInfoItemDTO.setPendingSentences(unreviewedAudios);
            return batchInfoItemDTO;
        }).sorted((e1, e2) -> (!e1.getAudioReviewed() && e2.getAudioReviewed()) ? -1 : ((e1.getAudioReviewed() && !e2.getAudioReviewed()) ? 1 : 0)).collect(Collectors.toList());
        return new BatchInfoDTO(sortedBatchDetails, null);
    }

    public BatchInfoDTO getBatchDetailsByTask(Long userId, Task task) {
        log.info("TASK....{}", task);
        switch (task) {
            case translation:
                return getTranslatorBatchDetails(userId);
            case review:
                return getReviewerBatchDetails(userId);
            case expertReview:
                return getExpertReviewerBatchDetails(userId);
            case audioRecording:
                return getAudioRecorderBatchDetails(userId);
            case audioReviewing:
                return getAudioReviewerBatchDetails(userId);
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

        List<VoiceEntity> approvedVoices = this.voiceRepo.findAllByStatusAndTranslatedSentenceBatchDetailsId(StatusTypes.approved, batchDetailsId);
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
        List<SentenceItemDto> sentenceItems = batchDetailsRepo.getAllSentencesInBatchDetails(batchDetailsId);
        logger.info("Number of sentence items retrieved: {}", sentenceItems.size());

        List<CompletedSentenceItemDto> sentences = getSentencesWithPresignedAudioUrl(sentenceItems);
        logger.info("Number of completed sentences with presigned audio URL: {}", sentences.size());

        // Add recordedBy information to each CompletedSentenceItemDto
        for (CompletedSentenceItemDto sentence : sentences) {
            logger.info("Adding recordedBy info for sentence with translatedSentenceId: {}", sentence.getTranslatedSentenceId());
            UserDetailDTO recordedBy = getVoiceDetailsByTranslatedSentenceId(sentence.getTranslatedSentenceId());
            if (recordedBy == null) {
                logger.warn("recordedBy is null for translatedSentenceId: {}", sentence.getTranslatedSentenceId());
            }
            sentence.setRecordedBy(recordedBy);
        }

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

    public UserDetailDTO getVoiceDetailsByTranslatedSentenceId(Long translatedSentenceId) {
        System.out.println("Entering getVoiceDetailsByTranslatedSentenceId with translatedSentenceId: " + translatedSentenceId);

        List<Object[]> result = translatedSentenceRepository.fetchVoiceDetailsByTranslatedSentenceId(translatedSentenceId);

        if (result == null || result.isEmpty()) {
            System.out.println("No voice details found for translatedSentenceId: " + translatedSentenceId);
            return null;
        }

        Object[] row = result.get(0);
        if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
            System.out.println("Invalid data in voice details for translatedSentenceId: " + translatedSentenceId);
            return null;
        }

        String fileUrl = (String) row[0];
        Long userId = ((Number) row[1]).longValue();

        System.out.println("fileUrl: " + fileUrl);
        System.out.println("userId: " + userId);

        UserDetailDTO userDetailDto = new UserDetailDTO();
        userDetailDto.setUserId(userId);
        userDetailDto.setFileUrl(fileUrl);

        return userDetailDto;
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
        if (batchDetails.getBatchStatus().ordinal() < BatchStatus.translated.ordinal()) {
            batchDetails.setBatchStatus(BatchStatus.translated);
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
        if (batchDetails.getBatchStatus().ordinal() < BatchStatus.translationVerified.ordinal()) {
            Optional<BatchDetailsStatsEntity> optionalTranslatorStats = this.batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetailsId);
            if (optionalTranslatorStats.isPresent()) {
                Integer rejectedSentences = this.translatedSentenceRepo.countAllByBatchDetailsIdAndReviewStatus(batchDetailsId, StatusTypes.rejected);
                if (rejectedSentences <= 0) {
                    batchDetails.setBatchStatus(BatchStatus.translationVerified);
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
            if (batchDetails.getBatchStatus().ordinal() < BatchStatus.translationVerified.ordinal() && batchDetails.getBatchStatus().ordinal() >= BatchStatus.translated.ordinal()) {
                Optional<BatchDetailsStatsEntity> optionalTranslatorStats = this.batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetailsId);
                if (optionalTranslatorStats.isPresent()) {
                    Integer approvedSentences = this.translatedSentenceRepo.countAllByBatchDetailsIdAndReviewStatus(batchDetailsId, StatusTypes.approved);
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
        if (batchDetails.getBatchStatus().ordinal() < BatchStatus.secondVerificationDone.ordinal()) {
            Optional<BatchDetailsStatsEntity> optionalUsersStats = this.batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetailsId);
            if (optionalUsersStats.isPresent()) {
                Integer rejectedSentences = this.translatedSentenceRepo.countAllByBatchDetailsIdAndSecondReview(batchDetailsId, StatusTypes.rejected);
                if (rejectedSentences <= 0) {
                    batchDetails.setBatchStatus(BatchStatus.secondVerificationDone);
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
        if (batchDetails.getBatchStatus().ordinal() < BatchStatus.recorded.ordinal()) {
            List<TranslatedSentenceEntity> voiceTasks = this.voiceService.findVoiceTasks(batchDetailsId);
            if (voiceTasks.isEmpty()) {
                batchDetails.setBatchStatus(BatchStatus.recorded);
                this.batchDetailsRepo.save(batchDetails);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.ok(new ResponseMessage("Translations marked as recorded"));
    }

    @Transactional
    public ResponseEntity<ResponseMessage> markAudioReviewAsComplete(Long batchDetailsId) {
        HashMap<String, Object> result = getBatchDetails(batchDetailsId);
        ResponseEntity<ResponseMessage> response = (ResponseEntity<ResponseMessage>) result.get("response");
        if (response != null)
            return response;
        BatchDetailsEntity batchDetails = (BatchDetailsEntity) result.get("batchDetails");
        if (batchDetails == null)
            return ResponseEntity.internalServerError().body(new ResponseMessage("An error occurred"));
        if (batchDetails.getBatchStatus().ordinal() < BatchStatus.audioVerified.ordinal()) {
            batchDetails.setBatchStatus(BatchStatus.audioVerified);
            this.batchDetailsRepo.save(batchDetails);
        }
        return ResponseEntity.ok(new ResponseMessage("Audios marked as verified"));
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
        List<CompletedSentenceItemDto> expertReviewedSentences = getSentencesWithPresignedAudioUrl(this.batchDetailsRepo.getAllSentencesInLanguagePerBatchDetailsStatus(languageId, BatchStatus.secondVerificationDone.ordinal()));
        return ResponseEntity.ok().body(new ExpertReviewedSentencesDto(language
                .get().getName(), expertReviewedSentences));
    }

    public List<CompletedSentenceItemDto> getSentencesWithPresignedAudioUrl(List<SentenceItemDto> sentences) {
        return sentences.stream().map(sentenceItemDto -> {
            System.out.println("Processing sentenceItem with translatedSentenceId: " + sentenceItemDto.getTranslatedSentenceId());

            // Fetch the recordedBy information
            UserDetailDTO recordedBy = getVoiceDetailsByTranslatedSentenceId(sentenceItemDto.getTranslatedSentenceId());
            if (recordedBy == null) {
                System.out.println("recordedBy is null for translatedSentenceId: " + sentenceItemDto.getTranslatedSentenceId());
            }

            // Create the CompletedSentenceItemDto
            CompletedSentenceItemDto completedSentence = new CompletedSentenceItemDto(sentenceItemDto, recordedBy);

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
                    Integer rejectedSentences = this.translatedSentenceRepo.countRejectedSentences(element.getBatchDetailsId(), StatusTypes.rejected, StatusTypes.rejected);
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
                    Integer unreviewedSentences = this.translatedSentenceRepo.countAllByBatchDetailsIdAndReviewStatus(element.getBatchDetailsId(), StatusTypes.unreviewed);
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
                    Integer unreviewedSentences = this.translatedSentenceRepo.countAllByBatchDetailsIdAndSecondReview(element.getBatchDetailsId(), StatusTypes.unreviewed);
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
            batch.setDescription(String.format("User feedback from %s to %s", sourceLanguage.getName(), targetLanguage.getName()));
            batch.setDeletionStatus(DeletionStatus.NOT_DELETED);
            batch.setTargetLanguage(targetLanguage);
            batch.setSourceLanguage(sourceLanguage);
            batch = batchRepo.save(batch);
        }

        if (batchDetailsEntity == null) {
            batchDetailsEntity = new BatchDetailsEntity();
            batchDetailsEntity.setBatchId(batch.getBatchNo());
            batchDetailsEntity.setBatchStatus(BatchStatus.translated);
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
        translatedSentenceEntity.setReviewStatus(StatusTypes.unreviewed);
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

        boolean useLegacyImplementation = !role.equals(UserBatchRole.AUDIO_RECORDER);

        //delete existing assignments
        batchDetailsUserAssigmentRepo.deleteAllByBatchDetailsIdAndBatchRole(batchDetailsId, role);

        for (Long userId : assignmentDTO.getUserIds()) {
            createBatchUserAssignment(userId, batchDetailsId, batchDetailsEntity.getBatchId(), role);

            if (useLegacyImplementation) {
                switch (role) {
                    case TEXT_TRANSLATOR:
                        batchDetailsEntity.setTranslatedById(userId);
                        batchDetailsEntity.setBatchStatus(BatchStatus.assignedTranslator);
                        break;
                    case TEXT_VERIFIER:
                        batchDetailsEntity.setTranslationVerifiedById(userId);
                        batchDetailsEntity.setBatchStatus(BatchStatus.assignedTextVerifier);
                        break;
                    case EXPERT_TEXT_REVIEWER:
                        batchDetailsEntity.setSecondReviewerId(userId);
                        batchDetailsEntity.setBatchStatus(BatchStatus.assignedRecorder);
                        break;
                    case AUDIO_VERIFIER:
                        batchDetailsEntity.setAudioVerifiedById(userId);
                        batchDetailsEntity.setBatchStatus(BatchStatus.assignedAudioVerifier);
                        break;
                    /*case EXPERT_TEXT_REVIEWER:
                         batchDetailsEntity.*/
                    default:
                        log.info("NO ROLE....");

                }
                log.info("SAVING BATCH ENTITY....{}", batchDetailsEntity.getBatchDetailsId());
                batchDetailsRepo.save(batchDetailsEntity);
            }

        }

        return ResponseEntity.ok(true);
    }

    public ResponseMessage moveBatchAssignmentsFromLegacyTable() {
        List<BatchDetailsEntity> batchDetailsEntities = batchDetailsRepo.findAll();
        for (BatchDetailsEntity batchDetailsEntity : batchDetailsEntities) {

            Long batchDetailsId = batchDetailsEntity.getBatchDetailsId();
            Long batchId = batchDetailsEntity.getBatchId();

            if (batchDetailsEntity.getTranslatedById() != null)
                createBatchUserAssignment(batchDetailsEntity.getTranslatedById(), batchDetailsId, batchId, UserBatchRole.TEXT_TRANSLATOR);

            if (batchDetailsEntity.getTranslationVerifiedById() != null)
                createBatchUserAssignment(batchDetailsEntity.getTranslationVerifiedById(), batchDetailsId, batchId, UserBatchRole.TEXT_VERIFIER);

            if (batchDetailsEntity.getSecondReviewerId() != null)
                createBatchUserAssignment(batchDetailsEntity.getSecondReviewerId(), batchDetailsId, batchId, UserBatchRole.EXPERT_TEXT_REVIEWER);

            if (batchDetailsEntity.getRecordedById() != null)
                createBatchUserAssignment(batchDetailsEntity.getRecordedById(), batchDetailsId,batchId, UserBatchRole.AUDIO_RECORDER);

            if (batchDetailsEntity.getAudioVerifiedById() != null)
                createBatchUserAssignment(batchDetailsEntity.getAudioVerifiedById(), batchDetailsId, batchId, UserBatchRole.AUDIO_VERIFIER);

        }
        return new ResponseMessage("Successfully moved legacy table");
    }

    private void createBatchUserAssignment(Long userId, Long batchDetailsId, Long batchId, UserBatchRole role) {
        BatchDetailsUserAssignment batchDetailsUserAssignment = new BatchDetailsUserAssignment();
        batchDetailsUserAssignment.setUserId(userId);
        batchDetailsUserAssignment.setBatchDetailsId(batchDetailsId);
        batchDetailsUserAssignment.setBatchId(batchId);
        batchDetailsUserAssignment.setBatchRole(role);
        batchDetailsUserAssigmentRepo.save(batchDetailsUserAssignment);
    }
}

