package kmusau.translator.DTOs.stats;

import kmusau.translator.entity.BatchDetailsEntity;
import kmusau.translator.entity.BatchDetailsStatsEntity;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class RoleStatsDto {

    private Long userId;
    private String username;
    private String email;
    private List<Translator> translator;
    private List<Translator> transcriber;
    private List<Moderator> moderator;
    private List<Moderator> transcriptionModerator;
    private List<Expert> expert;
    private List<Expert> transcriptionExpert;
    private List<Recorder> recorder;
    private List<AudioModerator> audioModerator;

    public RoleStatsDto(){

    }

    public RoleStatsDto(
            List<BatchDetailsStatsEntity> translatorStats,
            List<BatchDetailsStatsEntity> transcriberStats,
            List<BatchDetailsStatsEntity> moderatorStats,
            List<BatchDetailsStatsEntity> transcriptionModeratorStats,
            List<BatchDetailsStatsEntity> expertStats,
            List<BatchDetailsStatsEntity> transcriptionExpertStats,
            List<BatchDetailsStatsEntity> recorderStats,
            List<BatchDetailsStatsEntity> audioModeratorStats,
            Long userId,
            String username,
            String email
    ) {
        translator = translatorStats.stream()
                .map(Translator::entityToDto)
                .collect(Collectors.toList());

        transcriber = transcriberStats.stream()
                .map(Translator::entityToDto)
                .collect(Collectors.toList());

        moderator = moderatorStats.stream()
                .map(Moderator::entityToDto)
                .collect(Collectors.toList());

        transcriptionModerator = transcriptionModeratorStats.stream()
                .map(Moderator::entityToDto)
                .collect(Collectors.toList());

        expert = expertStats.stream()
                .map(Expert::entityToDto)
                .collect(Collectors.toList());

        transcriptionExpert = transcriptionExpertStats.stream()
                .map(Expert::entityToDto)
                .collect(Collectors.toList());

        recorder = recorderStats.stream()
                .map(Recorder::entityToDto)
                .collect(Collectors.toList());

        audioModerator = audioModeratorStats.stream()
                .map(AudioModerator::entityToDto)
                .collect(Collectors.toList());

        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    @Data
    public static class Translator{
        private Long batchDetailsId;
        private String source;
        private int totalSentences;
        private int sentencesTranslated;
        private int sentencesApproved;
        private int sentencesRejected;

        public static Translator entityToDto(BatchDetailsStatsEntity batchDetailsStatsEntity) {
            Translator translator1 = new Translator();
            BatchDetailsEntity batchDetails = batchDetailsStatsEntity.getBatchDetails();
            translator1.batchDetailsId = batchDetails.getBatchDetailsId();
            translator1.source = batchDetails.getBatch().getSource();
            translator1.totalSentences = batchDetails.getBatch().getSentences().size();
            translator1.sentencesTranslated = batchDetailsStatsEntity.getSentencesTranslated();
            translator1.sentencesApproved = batchDetailsStatsEntity.getSentencesApproved();
            translator1.sentencesRejected = batchDetailsStatsEntity.getSentencesRejected();
            return translator1;
        }
    }

    @Data
    public static class Moderator{
        private Long batchDetailsId;
        private String source;
        private int totalSentences;
        private int sentencesApproved;
        private int sentencesRejected;

        public static Moderator entityToDto(BatchDetailsStatsEntity batchDetailsStatsEntity) {
            Moderator moderator1 = new Moderator();
            BatchDetailsEntity batchDetails = batchDetailsStatsEntity.getBatchDetails();
            moderator1.batchDetailsId = batchDetails.getBatchDetailsId();
            moderator1.source = batchDetails.getBatch().getSource();
            moderator1.totalSentences = batchDetails.getBatch().getSentences().size();
            moderator1.sentencesApproved = batchDetailsStatsEntity.getSentencesApproved();
            moderator1.sentencesRejected = batchDetailsStatsEntity.getSentencesRejected();
            return moderator1;
        }
    }

    @Data
    public static class Expert{
        private Long batchDetailsId;
        private String source;
        private int totalSentences;
        private int sentencesExpertApproved;
        private int sentencesExpertRejected;

        public static Expert entityToDto(BatchDetailsStatsEntity batchDetailsStatsEntity) {
            Expert expert1 = new Expert();
            BatchDetailsEntity batchDetails = batchDetailsStatsEntity.getBatchDetails();
            expert1.batchDetailsId = batchDetails.getBatchDetailsId();
            expert1.source = batchDetails.getBatch().getSource();
            expert1.totalSentences = batchDetails.getBatch().getSentences().size();
            expert1.sentencesExpertApproved =  batchDetailsStatsEntity.getSentencesExpertApproved();
            expert1.sentencesExpertRejected = batchDetailsStatsEntity.getSentencesExpertRejected();
            return expert1;
        }
    }

    @Data
    public static class Recorder{
        private Long batchDetailsId;
        private String source;
        private int totalSentences;
        private int audiosRecorded;
        private int audiosApproved;
        private int audiosRejected;

        public static Recorder entityToDto(BatchDetailsStatsEntity batchDetailsStatsEntity) {
            Recorder recorder1 = new Recorder();
            BatchDetailsEntity batchDetails = batchDetailsStatsEntity.getBatchDetails();
            recorder1.batchDetailsId = batchDetails.getBatchDetailsId();
            recorder1.source = batchDetails.getBatch().getSource();
            recorder1.totalSentences = batchDetails.getBatch().getSentences().size();
            recorder1.audiosRecorded = batchDetailsStatsEntity.getAudiosRecorded();
            recorder1.audiosApproved = batchDetailsStatsEntity.getAudiosApproved();
            recorder1.audiosRejected = batchDetailsStatsEntity.getAudiosRejected();
            return recorder1;
        }
    }

    @Data
    public static class AudioModerator{
        private Long batchDetailsId;
        private String source;
        private int totalSentences;
        private int audiosApproved;
        private int audiosRejected;

        public static AudioModerator entityToDto(BatchDetailsStatsEntity batchDetailsStatsEntity) {
            AudioModerator audioModerator1 = new AudioModerator();
            BatchDetailsEntity batchDetails = batchDetailsStatsEntity.getBatchDetails();
            audioModerator1.batchDetailsId = batchDetails.getBatchDetailsId();
            audioModerator1.source = batchDetails.getBatch().getSource();
            audioModerator1.totalSentences = batchDetails.getBatch().getSentences().size();
            audioModerator1.audiosApproved = batchDetailsStatsEntity.getAudiosApproved();
            audioModerator1.audiosRejected = batchDetailsStatsEntity.getAudiosRejected();
            return audioModerator1;
        }
    }
}

