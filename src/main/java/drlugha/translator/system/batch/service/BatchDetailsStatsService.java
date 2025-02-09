package drlugha.translator.system.batch.service;

import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.shared.enums.StatusTypes;
import drlugha.translator.shared.exception.BadRequestException;
import drlugha.translator.shared.exception.GeneralException;
import drlugha.translator.system.batch.enums.BatchStatus;
import drlugha.translator.system.batch.enums.BatchType;
import drlugha.translator.system.batch.model.BatchDetailsEntity;
import drlugha.translator.system.batch.model.BatchDetailsStatsEntity;
import drlugha.translator.system.batch.projections.*;
import drlugha.translator.system.batch.repository.BatchDetailsRepository;
import drlugha.translator.system.batch.repository.BatchDetailsStatsRepository;
import drlugha.translator.system.sentence.repository.TranslatedSentenceRepository;
import drlugha.translator.system.stats.dto.*;
import drlugha.translator.system.user.model.User;
import drlugha.translator.system.user.repository.UserRepository;
import drlugha.translator.system.voice.repository.VoiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BatchDetailsStatsService {

    public static final String START_OF_DAY = " 00:00:00";
    public static final String END_OF_DAY = " 23:59:59";
    private final BatchDetailsStatsRepository batchDetailsStatsRepository;

    private final UserRepository userRepository;

    private final BatchDetailsRepository batchDetailsRepository;

    private final TranslatedSentenceRepository translatedSentenceRepository;

    private final VoiceRepository voiceRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    public BatchDetailsStatsService(
            BatchDetailsStatsRepository batchDetailsStatsRepository,
            UserRepository userRepository,
            BatchDetailsRepository batchDetailsRepository,
            TranslatedSentenceRepository translatedSentenceRepository,
            VoiceRepository voiceRepository, EntityManager entityManager
    ) {
        this.batchDetailsStatsRepository = batchDetailsStatsRepository;
        this.userRepository = userRepository;
        this.batchDetailsRepository = batchDetailsRepository;
        this.translatedSentenceRepository = translatedSentenceRepository;
        this.voiceRepository = voiceRepository;
        this.entityManager = entityManager;
    }

    public BatchDetailsStatsEntity getBatchDetailsStatsById(Long batchDetailsId) {
        if (batchDetailsId == null) {
            throw new BadRequestException("Please provide batch details id");
        }

        Optional<BatchDetailsEntity> batchDetails = batchDetailsRepository.findById(batchDetailsId);
        if (batchDetails.isEmpty()) {
            throw new BadRequestException("Batch details id not found");
        }

        Optional<BatchDetailsStatsEntity> optionalUserStats = batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetailsId);
        return optionalUserStats.orElseGet(BatchDetailsStatsEntity::new);

    }

    public Page<BatchDetailsStats> getBatchDetailsStats(String batchTypeString, Integer page, Integer pageSize, Long languageId, BatchStatus status) {
        BatchType batchType = BatchType.fromName(batchTypeString).orElse(BatchType.TEXT);

        Pageable pageable = PageRequest.of(page, pageSize);

        Page<BatchDetailsStatsMapping> batchDetailsStatsMappings = null;

        Integer statusOrdinal =  status != null ? status.ordinal() : null;

        if (batchType == BatchType.AUDIO)
            batchDetailsStatsMappings = batchDetailsStatsRepository.getBatchDetailsStatsAudio(pageable, languageId, statusOrdinal);
        else if (batchType == BatchType.TEXT)
            batchDetailsStatsMappings = batchDetailsStatsRepository.getBatchDetailsStatsText(pageable, languageId, statusOrdinal);
        else if (batchType == BatchType.TEXT_FEEDBACK)
            batchDetailsStatsMappings = batchDetailsStatsRepository.getBatchDetailsStatsFeedbackText(pageable, languageId, statusOrdinal);

        if (batchDetailsStatsMappings == null)
            return new PageImpl<>(Collections.emptyList());

        Map<Long, List<BatchDetailsStatsMapping>> listMap = batchDetailsStatsMappings.getContent().stream()
                .collect(Collectors.groupingBy(BatchDetailsStatsMapping::getBatchDetailsId));

        Map<Long, BatchDetailsStats> batchStatsMap = new HashMap<>();

        for (Map.Entry<Long, List<BatchDetailsStatsMapping>> entry : listMap.entrySet()) {
            BatchDetailsStats batchDetailsStats = batchStatsMap.computeIfAbsent(entry.getKey(), k -> {
                BatchDetailsStats newStats = new BatchDetailsStats();
                BatchDetailsStatsMapping mapping = entry.getValue().get(0); // Get first mapping entry

                newStats.setBatchDetailsId(entry.getKey());
                newStats.setSource(mapping.getSource());
                newStats.setLanguage(mapping.getLanguage());
                newStats.setBatchNo(mapping.getBatchNo());
                BatchStatus batchStatus = BatchStatus.values()[mapping.getStatus()];
                if (batchStatus != null)
                    newStats.setStatus(batchStatus.getLabel(batchType));
                newStats.setNumberOfSentences(mapping.getNumberOfSentences());
                newStats.setSentencesTranslated(mapping.getSentencesTranslated());
                newStats.setSentencesApproved(mapping.getSentencesApproved());
                newStats.setSentencesRejected(mapping.getSentencesRejected());
                newStats.setSentencesExpertApproved(mapping.getSentencesExpertApproved());
                newStats.setSentencesExpertRejected(mapping.getSentencesExpertRejected());
                newStats.setTranslator(mapping.getTranslator());
                newStats.setExpert(mapping.getExpert());
                newStats.setModerator(mapping.getModerator());
                newStats.setAudioModerator(mapping.getAudioModerator());

                return newStats;
            });

            // Add audio stats for each mapping
            for (BatchDetailsStatsMapping mapping : entry.getValue()) {
                BatchDetailsStats.AudioStats audioStats = new BatchDetailsStats.AudioStats();
                audioStats.setAudiosRecorded(mapping.getAudiosRecorded());
                audioStats.setRecorder(mapping.getRecorder());
                audioStats.setAudiosApproved(mapping.getAudiosApproved());
                audioStats.setAudiosRejected(mapping.getAudiosRejected());
                batchDetailsStats.getAudioStats().add(audioStats);
            }
        }

        // Convert List to Page
        List<BatchDetailsStats> batchDetailsStatsList = new ArrayList<>(batchStatsMap.values());
        return new PageImpl<>(batchDetailsStatsList, pageable, batchDetailsStatsMappings.getTotalElements());
    }

    public RoleStatsDTO findUsersStatsForEachBatchDetails(Long userId) {
        if (userId == null) {
            throw new BadRequestException("Please provide user id");
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new BadRequestException("User not found");
        }
        List<BatchDetailsStatsEntity> translatorStats = batchDetailsStatsRepository.findTranslatorStatsPerBatchDetails(userId, BatchType.TEXT.getName());
        List<BatchDetailsStatsEntity> transcriberStats = batchDetailsStatsRepository.findTranslatorStatsPerBatchDetails(userId, BatchType.AUDIO.getName());
        List<BatchDetailsStatsEntity> moderatorStats = batchDetailsStatsRepository.findModeratorStatsPerBatchDetails(userId, BatchType.TEXT.getName());
        List<BatchDetailsStatsEntity> transcriptionModeratorStats = batchDetailsStatsRepository.findModeratorStatsPerBatchDetails(userId, BatchType.AUDIO.getName());
        List<BatchDetailsStatsEntity> expertsStats = batchDetailsStatsRepository.findExpertStatsPerBatchDetails(userId, BatchType.TEXT.getName());
        List<BatchDetailsStatsEntity> transcriptionExpertStats = batchDetailsStatsRepository.findExpertStatsPerBatchDetails(userId, BatchType.AUDIO.getName());
        List<BatchDetailsStatsEntity> recorderStats = batchDetailsStatsRepository.findRecorderStatsPerBatchDetails(userId);
        List<BatchDetailsStatsEntity> audioModeratorStats = batchDetailsStatsRepository.findAudioModeratorStatsPerBatchDetails(userId);

        return new RoleStatsDTO(
                translatorStats, transcriberStats, moderatorStats, transcriptionModeratorStats, expertsStats, transcriptionExpertStats, recorderStats, audioModeratorStats, user.get().getUserId(),
                user.get().getUsername(), user.get().getEmail()
        );
    }

    public UserStatsDTO findTotalUserStats(Long userId) {
        if (userId == null) {
            throw new BadRequestException("Please provide user id");
        }

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new BadRequestException("User not found");
        }
        TranslatorStats translatorStats = batchDetailsStatsRepository.findTranslatorStats(userId, BatchType.TEXT.getName());
        TranslatorStats transcriberStats = batchDetailsStatsRepository.findTranslatorStats(userId, BatchType.AUDIO.getName());
        ModeratorStats moderatorStats = batchDetailsStatsRepository.findModeratorStats(userId, BatchType.TEXT.getName());
        ModeratorStats transcriptionModeratorStats = batchDetailsStatsRepository.findModeratorStats(userId, BatchType.AUDIO.getName());
        ExpertStats expertsStats = batchDetailsStatsRepository.findExpertsStats(userId, BatchType.TEXT.getName());
        ExpertStats transcriptionExpertsStats = batchDetailsStatsRepository.findExpertsStats(userId, BatchType.AUDIO.getName());
        RecorderStats recorderStats = batchDetailsStatsRepository.findRecorderStats(userId);
        AudioModeratorStats audioModeratorStats = batchDetailsStatsRepository.findAudioModeratorStats(userId);

        UserStatsDTO userStatsDto = new UserStatsDTO();
        userStatsDto.setUserId(userId);
        userStatsDto.setUsername(user.get().getUsername());
        userStatsDto.setEmail(user.get().getEmail());
        userStatsDto.setTranslator(translatorStats);
        userStatsDto.setTranscriber(transcriberStats);
        userStatsDto.setModerator(moderatorStats);
        userStatsDto.setTranscriptionModerator(transcriptionModeratorStats);
        userStatsDto.setExpert(expertsStats);
        userStatsDto.setTranscriptionExpert(transcriptionExpertsStats);
        userStatsDto.setRecorder(recorderStats);
        userStatsDto.setAudioModerator(audioModeratorStats);
        return userStatsDto;
    }

    public List<UserStatsDTO> findAllUsersStats(String batchType) {
        if (Strings.isBlank(batchType)) {
            batchType = BatchType.TEXT.getName();
        }
        Map<Long, TranslatorStats> allTranslatorsStats = batchDetailsStatsRepository.findAllTranslatorsStats(batchType)
                .stream()
                .collect(Collectors.toMap(TranslatorStats::getUserId, Function.identity()));

        Map<Long, ModeratorStats> allModeratorStats = batchDetailsStatsRepository.findAllModeratorStats(batchType)
                .stream()
                .collect(Collectors.toMap(ModeratorStats::getUserId, Function.identity()));

        Map<Long, ExpertStats> allExpertStats = batchDetailsStatsRepository.findAllExpertStats(batchType)
                .stream()
                .collect(Collectors.toMap(ExpertStats::getUserId, Function.identity()));

        Map<Long, RecorderStats> allRecorderStats = batchDetailsStatsRepository.findAllRecorderStats(batchType)
                .stream()
                .collect(Collectors.toMap(RecorderStats::getUserId, Function.identity()));

        Map<Long, AudioModeratorStats> allAudioModeratorStats = batchDetailsStatsRepository.findAllAudioModeratorStats(batchType)
                .stream()
                .collect(Collectors.toMap(AudioModeratorStats::getUserId, Function.identity()));

        List<User> allUsers = userRepository.findAll();

        List<UserStatsDTO> userStatsDTOS = allUsers.stream()
                .map(UserStatsDTO::new)
                .collect(Collectors.toList());

        for (UserStatsDTO dto : userStatsDTOS) {
            dto.setTranslator(allTranslatorsStats.get(dto.getUserId()));
            dto.setModerator(allModeratorStats.get(dto.getUserId()));
            dto.setExpert(allExpertStats.get(dto.getUserId()));
            dto.setRecorder(allRecorderStats.get(dto.getUserId()));
            dto.setAudioModerator(allAudioModeratorStats.get(dto.getUserId()));
        }
        return userStatsDTOS;
    }

    @Transactional(timeout = 1000 * 60 * 20)
    public ResponseMessage populateStatsForExistingBatches() {
        List<BatchDetailsEntity> batchDetails = batchDetailsRepository.findAll();
        ArrayList<BatchDetailsStatsEntity> batchDetailsStatsEntities = new ArrayList<>();
        for (BatchDetailsEntity batchDetail : batchDetails) {
            Optional<BatchDetailsStatsEntity> optionalBatchDetailsStats = batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetail.getBatchDetailsId());
            if (optionalBatchDetailsStats.isPresent())
                continue;
            BatchDetailsStatsEntity batchDetailsStats = new BatchDetailsStatsEntity();
            int translatedSentences = batchDetail.getTranslatedSentence().size();

            int approvedTranslations = translatedSentenceRepository.countAllByBatchDetailsIdAndReviewStatus(batchDetail.getBatchDetailsId(), StatusTypes.APPROVED);

            int rejectedTranslations = translatedSentenceRepository.countAllByBatchDetailsIdAndReviewStatus(batchDetail.getBatchDetailsId(), StatusTypes.REJECTED);

            int expertApprovedTranslations = translatedSentenceRepository.countAllByBatchDetailsIdAndSecondReview(batchDetail.getBatchDetailsId(), StatusTypes.APPROVED);
            int expertRejectedTranslations = translatedSentenceRepository.countAllByBatchDetailsIdAndSecondReview(batchDetail.getBatchDetailsId(), StatusTypes.REJECTED);

            int audiosRecorded = voiceRepository.countAllByTranslatedSentenceBatchDetails_BatchDetailsId(batchDetail.getBatchDetailsId());

            int approvedAudios =
                    voiceRepository.countAllByStatusAndTranslatedSentenceBatchDetailsId(
                            StatusTypes.APPROVED, batchDetail.getBatchDetailsId()
                    );

            int rejectedAudios = voiceRepository.countAllByStatusAndTranslatedSentenceBatchDetailsId(
                    StatusTypes.REJECTED, batchDetail.getBatchDetailsId());

            batchDetailsStats.setSentencesTranslated(translatedSentences);
            batchDetailsStats.setSentencesApproved(approvedTranslations);
            batchDetailsStats.setSentencesRejected(rejectedTranslations);
            batchDetailsStats.setSentencesExpertApproved(expertApprovedTranslations);
            batchDetailsStats.setSentencesExpertRejected(expertRejectedTranslations);
            batchDetailsStats.setAudiosRecorded(audiosRecorded);
            batchDetailsStats.setAudiosApproved(approvedAudios);
            batchDetailsStats.setAudiosRejected(rejectedAudios);
            batchDetailsStats.setBatchDetails(batchDetail);

            batchDetailsStatsEntities.add(batchDetailsStats);
        }

        batchDetailsStatsRepository.saveAll(batchDetailsStatsEntities);

        return new ResponseMessage("Batch details successfully populated");
    }

    public TotalsDTO getTotalSentencesAndTranslatedSentences() {
        TotalSentencesDto totalSentences = batchDetailsRepository.getTotalSentences();
        TotalTranslatedSentencesDto totalTranslatedSentences = batchDetailsRepository.getTotalTranslatedSentences();
        return new TotalsDTO(totalSentences, totalTranslatedSentences);
    }

    public List<TotalUserStatsDto> getTotalUserStats(String batchType, String startDateString, String endDateString) {
        try {
            if (Strings.isBlank(batchType)) {
                batchType = BatchType.TEXT.getName();
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startDate = simpleDateFormat.parse(startDateString + START_OF_DAY);
            Date endDate = simpleDateFormat.parse(endDateString + END_OF_DAY);
            String timeZone = "+00:00";
            if (ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now()).toString().contains("+03:00"))
                timeZone = "+03:00";

            return batchDetailsStatsRepository.getTotalUserStats(batchType, startDate, endDate, timeZone);
        } catch (Exception exception) {
            throw new GeneralException(exception.getMessage());
        }
    }
}
