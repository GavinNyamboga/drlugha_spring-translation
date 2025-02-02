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

    public List<BatchDetailsStats> getBatchDetailsStats(String batchTypeString) {
        BatchType batchType = BatchType.fromName(batchTypeString).orElse(BatchType.TEXT);

        List<BatchDetailsStatsMapping> batchDetailsStatsMappings = new ArrayList<>();

        if (batchType == BatchType.AUDIO)
            batchDetailsStatsMappings = batchDetailsStatsRepository.getBatchDetailsStatsAudio();

        if (batchType == BatchType.TEXT)
            batchDetailsStatsMappings = batchDetailsStatsRepository.getBatchDetailsStatsText();

        if (batchType == BatchType.TEXT_FEEDBACK)
            batchDetailsStatsMappings = batchDetailsStatsRepository.getBatchDetailsStatsFeedbackText();

        Map<Long, List<BatchDetailsStatsMapping>> listMap = new HashMap<>();
        batchDetailsStatsMappings.forEach(item ->
                listMap.computeIfAbsent(item.getBatchDetailsId(), k -> new ArrayList<>())
                        .add(item)
        );

        Map<Long, BatchDetailsStats> batchStatsMap = new HashMap<>();

        for (Map.Entry<Long, List<BatchDetailsStatsMapping>> entry : listMap.entrySet()) {
            BatchDetailsStats batchDetailsStats = null;

            for (BatchDetailsStatsMapping mapping : entry.getValue()) {
                if (batchStatsMap.containsKey(entry.getKey())) {
                    // If we already have this batch, just add the audio stats
                    batchDetailsStats = batchStatsMap.get(entry.getKey());
                } else {
                    // Create new batch details stats
                    batchDetailsStats = new BatchDetailsStats();
                    batchDetailsStats.setBatchDetailsId(entry.getKey());
                    batchDetailsStats.setSource(mapping.getSource());
                    batchDetailsStats.setLanguage(mapping.getLanguage());
                    batchDetailsStats.setBatchNo(mapping.getBatchNo());
                    BatchStatus batchStatus = BatchStatus.values()[mapping.getStatus()];
                    if (batchStatus != null)
                        batchDetailsStats.setStatus(batchStatus.getLabel(batchType));
                    batchDetailsStats.setNumberOfSentences(mapping.getNumberOfSentences());
                    batchDetailsStats.setSentencesTranslated(mapping.getSentencesTranslated());
                    batchDetailsStats.setSentencesApproved(mapping.getSentencesApproved());
                    batchDetailsStats.setSentencesRejected(mapping.getSentencesRejected());
                    batchDetailsStats.setSentencesExpertApproved(mapping.getSentencesExpertApproved());
                    batchDetailsStats.setSentencesExpertRejected(mapping.getSentencesExpertRejected());
                    batchDetailsStats.setTranslator(mapping.getTranslator());
                    batchDetailsStats.setExpert(mapping.getExpert());
                    batchDetailsStats.setModerator(mapping.getModerator());
                    batchDetailsStats.setAudioModerator(mapping.getAudioModerator());

                    batchStatsMap.put(entry.getKey(), batchDetailsStats);
                }

                // Add audio stats for each mapping
                BatchDetailsStats.AudioStats audioStats = new BatchDetailsStats.AudioStats();
                audioStats.setAudiosRecorded(mapping.getAudiosRecorded());
                audioStats.setRecorder(mapping.getRecorder());
                audioStats.setAudiosApproved(mapping.getAudiosApproved());
                audioStats.setAudiosRejected(mapping.getAudiosRejected());
                batchDetailsStats.getAudioStats().add(audioStats);
            }
        }

        return new ArrayList<>(batchStatsMap.values());
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

            int approvedTranslations = translatedSentenceRepository.countAllByBatchDetailsIdAndReviewStatus(batchDetail.getBatchDetailsId(), StatusTypes.approved);

            int rejectedTranslations = translatedSentenceRepository.countAllByBatchDetailsIdAndReviewStatus(batchDetail.getBatchDetailsId(), StatusTypes.rejected);

            int expertApprovedTranslations = translatedSentenceRepository.countAllByBatchDetailsIdAndSecondReview(batchDetail.getBatchDetailsId(), StatusTypes.approved);
            int expertRejectedTranslations = translatedSentenceRepository.countAllByBatchDetailsIdAndSecondReview(batchDetail.getBatchDetailsId(), StatusTypes.rejected);

            int audiosRecorded = voiceRepository.countAllByTranslatedSentenceBatchDetails_BatchDetailsId(batchDetail.getBatchDetailsId());

            int approvedAudios =
                    voiceRepository.countAllByStatusAndTranslatedSentenceBatchDetailsId(
                            StatusTypes.approved, batchDetail.getBatchDetailsId()
                    );

            int rejectedAudios = voiceRepository.countAllByStatusAndTranslatedSentenceBatchDetailsId(
                    StatusTypes.rejected, batchDetail.getBatchDetailsId());

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
