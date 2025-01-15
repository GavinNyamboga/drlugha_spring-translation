package kmusau.translator.service;

import kmusau.translator.DTOs.stats.*;
import kmusau.translator.entity.BatchDetailsEntity;
import kmusau.translator.entity.BatchDetailsStatsEntity;
import kmusau.translator.entity.UsersEntity;
import kmusau.translator.enums.BatchType;
import kmusau.translator.enums.StatusTypes;
import kmusau.translator.projections.*;
import kmusau.translator.repository.*;
import kmusau.translator.response.ResponseMessage;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BatchDetailsStatsService {

    public static final String START_OF_DAY = " 00:00:00";
    public static final String END_OF_DAY = " 23:59:59";
    private final BatchDetailsStatsRepository batchDetailsStatsRepository;

    private final UserRepository userRepository;

    private final BatchDetailsRepository batchDetailsRepository;

    private final TranslatedSentenceRepository translatedSentenceRepository;

    private final VoiceRepository voiceRepository;

    public BatchDetailsStatsService(
            BatchDetailsStatsRepository batchDetailsStatsRepository,
            UserRepository userRepository,
            BatchDetailsRepository batchDetailsRepository,
            TranslatedSentenceRepository translatedSentenceRepository,
            VoiceRepository voiceRepository
    ) {
        this.batchDetailsStatsRepository = batchDetailsStatsRepository;
        this.userRepository = userRepository;
        this.batchDetailsRepository = batchDetailsRepository;
        this.translatedSentenceRepository = translatedSentenceRepository;
        this.voiceRepository = voiceRepository;
    }

    public ResponseEntity getBatchDetailsStatsById(Long batchDetailsId) {
        if (batchDetailsId == null){
            return ResponseEntity.badRequest().body(new ResponseMessage("Please provide batch details id"));
        }

        Optional<BatchDetailsEntity> batchDetails = batchDetailsRepository.findById(batchDetailsId);
        if (batchDetails.isEmpty()){
            return ResponseEntity.badRequest().body(new ResponseMessage("Batch details id not found"));
        }

        Optional<BatchDetailsStatsEntity> optionalUserStats = batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetailsId);
        if (optionalUserStats.isEmpty())
            return ResponseEntity.ok().body(new BatchDetailsStats());

        BatchDetailsStatsEntity batchDetailsStatsEntity = optionalUserStats.get();
        return ResponseEntity.ok(BatchDetailsStats.entityToDto(batchDetailsStatsEntity));
    }

    public ResponseEntity getBatchDetailsStats(String batchTypeString) {
        Optional<BatchType> batchTypeOptional = BatchType.fromName(batchTypeString);
        BatchType batchType;
        if (batchTypeOptional.isEmpty()){
            batchType = BatchType.TEXT;
        }
        else {
            batchType = batchTypeOptional.get();
        }
        List<BatchDetailsStatsEntity> userStats = batchDetailsStatsRepository.findAllByBatchType(batchType, Sort.by("batchDetails.batchId", "batchDetails.language"));
        List<BatchDetailsStats> batchDetailsStats = userStats.stream()
                .map(BatchDetailsStats::entityToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(batchDetailsStats);
    }

    public ResponseEntity findUsersStatsForEachBatchDetails(Long userId) {
        if (userId == null){
            return ResponseEntity.badRequest().body(new ResponseMessage("Please provide user id"));
        }
        Optional<UsersEntity> user = userRepository.findById(userId);
        if (user.isEmpty()){
            return ResponseEntity.badRequest().body(new ResponseMessage("User not found"));
        }
        List<BatchDetailsStatsEntity> translatorStats = batchDetailsStatsRepository.findTranslatorStatsPerBatchDetails(userId, BatchType.TEXT.getName());
        List<BatchDetailsStatsEntity> transcriberStats = batchDetailsStatsRepository.findTranslatorStatsPerBatchDetails(userId, BatchType.AUDIO.getName());
        List<BatchDetailsStatsEntity> moderatorStats = batchDetailsStatsRepository.findModeratorStatsPerBatchDetails(userId, BatchType.TEXT.getName());
        List<BatchDetailsStatsEntity> transcriptionModeratorStats = batchDetailsStatsRepository.findModeratorStatsPerBatchDetails(userId, BatchType.AUDIO.getName());
        List<BatchDetailsStatsEntity> expertsStats = batchDetailsStatsRepository.findExpertStatsPerBatchDetails(userId, BatchType.TEXT.getName());
        List<BatchDetailsStatsEntity> transcriptionExpertStats = batchDetailsStatsRepository.findExpertStatsPerBatchDetails(userId, BatchType.AUDIO.getName());
        List<BatchDetailsStatsEntity> recorderStats = batchDetailsStatsRepository.findRecorderStatsPerBatchDetails(userId);
        List<BatchDetailsStatsEntity> audioModeratorStats = batchDetailsStatsRepository.findAudioModeratorStatsPerBatchDetails(userId);

        RoleStatsDto roleStatsDto = new RoleStatsDto(
                translatorStats, transcriberStats, moderatorStats, transcriptionModeratorStats, expertsStats, transcriptionExpertStats, recorderStats, audioModeratorStats, user.get().getUserId(),
                user.get().getUsername(), user.get().getEmail()
        );

        return ResponseEntity.ok(roleStatsDto);
    }

    public ResponseEntity findTotalUserStats(Long userId) {
        if (userId == null){
            return ResponseEntity.badRequest().body(new ResponseMessage("Please provide user id"));
        }

        Optional<UsersEntity> user = userRepository.findById(userId);
        if (user.isEmpty()){
            return ResponseEntity.badRequest().body(new ResponseMessage("User not found"));
        }
        TranslatorStats translatorStats = batchDetailsStatsRepository.findTranslatorStats(userId, BatchType.TEXT.getName());
        TranslatorStats transcriberStats = batchDetailsStatsRepository.findTranslatorStats(userId, BatchType.AUDIO.getName());
        ModeratorStats moderatorStats = batchDetailsStatsRepository.findModeratorStats(userId, BatchType.TEXT.getName());
        ModeratorStats transcriptionModeratorStats = batchDetailsStatsRepository.findModeratorStats(userId, BatchType.AUDIO.getName());
        ExpertStats expertsStats = batchDetailsStatsRepository.findExpertsStats(userId, BatchType.TEXT.getName());
        ExpertStats transcriptionExpertsStats = batchDetailsStatsRepository.findExpertsStats(userId, BatchType.AUDIO.getName());
        RecorderStats recorderStats = batchDetailsStatsRepository.findRecorderStats(userId);
        AudioModeratorStats audioModeratorStats = batchDetailsStatsRepository.findAudioModeratorStats(userId);

        UserStatsDto userStatsDto = new UserStatsDto();
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
        return ResponseEntity.ok(userStatsDto);
    }

    public ResponseEntity findAllUsersStats(String batchType) {
        if (Strings.isBlank(batchType)){
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

        List<UsersEntity> allUsers = userRepository.findAll();

        List<UserStatsDto> userStatsDtos = allUsers.stream()
                .map(UserStatsDto::new)
                .collect(Collectors.toList());

        for (UserStatsDto dto : userStatsDtos) {
            dto.setTranslator(allTranslatorsStats.get(dto.getUserId()));
            dto.setModerator(allModeratorStats.get(dto.getUserId()));
            dto.setExpert(allExpertStats.get(dto.getUserId()));
            dto.setRecorder(allRecorderStats.get(dto.getUserId()));
            dto.setAudioModerator(allAudioModeratorStats.get(dto.getUserId()));
        }
        return ResponseEntity.ok(userStatsDtos);
    }

    @Transactional(timeout = 1000 * 60 * 20)
    public ResponseEntity<ResponseMessage> populateStatsForExistingBatches() {
        List<BatchDetailsEntity> batchDetails = batchDetailsRepository.findAll();
        ArrayList<BatchDetailsStatsEntity> batchDetailsStatsEntities  = new ArrayList<>();
        for (BatchDetailsEntity batchDetail : batchDetails) {
            Optional<BatchDetailsStatsEntity> optionalBatchDetailsStats = batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetail.getBatchDetailsId());
            if (optionalBatchDetailsStats.isPresent())
                continue;
            BatchDetailsStatsEntity batchDetailsStats = new BatchDetailsStatsEntity();
            int translatedSentences = batchDetail.getTranslatedSentence().size();

            int approvedTranslations =
                    translatedSentenceRepository.countAllByBatchDetailsIdAndReviewStatus(batchDetail.getBatchDetailsId(), StatusTypes.approved);

            int rejectedTranslations =
                    translatedSentenceRepository.countAllByBatchDetailsIdAndReviewStatus(batchDetail.getBatchDetailsId(), StatusTypes.rejected);

            int expertApprovedTranslations =
                    translatedSentenceRepository.countAllByBatchDetailsIdAndSecondReview(batchDetail.getBatchDetailsId(), StatusTypes.approved);
            int expertRejectedTranslations =
                    translatedSentenceRepository.countAllByBatchDetailsIdAndSecondReview(batchDetail.getBatchDetailsId(), StatusTypes.rejected);

            int audiosRecorded =
                    voiceRepository.countAllByTranslatedSentenceBatchDetails_BatchDetailsId(batchDetail.getBatchDetailsId());

            int approvedAudios =
                    voiceRepository.countAllByStatusAndTranslatedSentenceBatchDetailsId(
                            StatusTypes.approved, batchDetail.getBatchDetailsId()
                    );

            int rejectedAudios =
                    voiceRepository.countAllByStatusAndTranslatedSentenceBatchDetailsId(
                            StatusTypes.rejected, batchDetail.getBatchDetailsId()
                    );

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

        return ResponseEntity.ok(new ResponseMessage("Batch details successfully populated"));
    }

    public ResponseEntity<TotalsDto> getTotalSentencesAndTranslatedSentences() {
        TotalSentencesDto totalSentences = batchDetailsRepository.getTotalSentences();
        TotalTranslatedSentencesDto totalTranslatedSentences = batchDetailsRepository.getTotalTranslatedSentences();
        TotalsDto totalsDto = new TotalsDto(totalSentences, totalTranslatedSentences);
        return ResponseEntity.ok(totalsDto);
    }

    public ResponseEntity<List<TotalUserStatsDto>> getTotalUserStats(String batchType, String startDateString, String endDateString) {
        try {
            if (Strings.isBlank(batchType)){
                batchType = BatchType.TEXT.getName();
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startDate = simpleDateFormat.parse(startDateString + START_OF_DAY);
            Date endDate = simpleDateFormat.parse(endDateString + END_OF_DAY);
            String timeZone = "+00:00";
            if (ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now()).toString().contains("+03:00"))
                timeZone = "+03:00";

            return ResponseEntity.ok(batchDetailsStatsRepository.getTotalUserStats(batchType, startDate, endDate, timeZone));
        }
        catch (Exception exception){
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
