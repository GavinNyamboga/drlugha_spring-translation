package drlugha.translator.system.voice.controller;

import drlugha.translator.configs.AmazonClient;
import drlugha.translator.shared.controller.BaseController;
import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.shared.enums.StatusTypes;
import drlugha.translator.system.batch.enums.BatchStatus;
import drlugha.translator.system.sentence.dto.SentenceToRecordDto;
import drlugha.translator.system.sentence.dto.VoicesToReviewDto;
import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.system.sentence.repository.TranslatedSentenceRepository;
import drlugha.translator.system.voice.model.VoiceEntity;
import drlugha.translator.system.voice.repository.VoiceRepository;
import drlugha.translator.system.voice.service.VoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AudioController extends BaseController {
    private final AmazonClient amazonClient;

    private final TranslatedSentenceRepository translatedRepo;

    private final VoiceService voiceSvc;

    private final VoiceRepository voiceRepo;

    @PutMapping({"/assign/audiotasks/{recorderId}/{audioReviewerId}"})
    public ResponseMessage assignAudioRecorderAndAudioReviewer(@RequestBody List<TranslatedSentenceEntity> translatedSentences, @PathVariable Long recorderId, @PathVariable Long audioReviewerId) {
        if (recorderId == null || audioReviewerId == null)
            return new ResponseMessage("Recorder Id or Audio-Reviewer Id cannot be empty");
        try {
            translatedSentences.forEach(translatedSentenceEntity -> translatedSentenceEntity.setAssignedRecorderId(recorderId));
            translatedSentences.forEach(translatedSentenceEntity -> translatedSentenceEntity.setAssignedAudioReviewerId(audioReviewerId));
            translatedSentences.forEach(translatedSentenceEntity -> translatedSentenceEntity.setRecordedStatus(StatusTypes.ASSIGNED));
            translatedRepo.saveAll(translatedSentences);
            return new ResponseMessage("Updated list successfully");
        } catch (Exception e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @GetMapping({"users/audio/assignments"})
    public List<TranslatedSentenceEntity> getAudioAssignments(@RequestParam(defaultValue = "assigned") StatusTypes recordedStatus, @RequestParam Long userId) {
        return translatedRepo.findByRecordedStatusAndAssignedRecorderUserId(recordedStatus, userId);
    }

    @GetMapping({"/recorder/tasks"})
    public ResponseEntity<SentenceToRecordDto> usersAudioTasks(@RequestParam(name = "status", defaultValue = "ASSIGNED_RECORDER") BatchStatus batchStatus,
                                                               @RequestParam Long recorderId,
                                                               @RequestParam(required = false) Long batchDetailsId) {
        return voiceSvc.recorderAssignedTasks(recorderId, batchStatus, batchDetailsId);
    }

    @GetMapping({"/fetch/voice/{id}"})
    public String getSingleAudio(@PathVariable Long id) {
        return amazonClient.getSingleAudio(id);
    }

    @GetMapping({"/approved/voice"})
    public List<VoiceEntity> getVoiceByStatus(@RequestParam(defaultValue = "approved") StatusTypes status) {
        return voiceSvc.getVoiceByStatus(status);
    }

    @GetMapping({"/recorder/audiotasks"})
    public List<TranslatedSentenceEntity> getUsersAudioTasks(@RequestParam(defaultValue = "unreviewed") StatusTypes status, @RequestParam Long userId) {
        return translatedRepo.findByRecordedStatusAndAssignedRecorderUserId(status, userId);
    }

    @GetMapping({"/reviewer/audio"})
    public ResponseEntity<VoicesToReviewDto> getVoicesToReview(@RequestParam Long userId, @RequestParam(required = false) Long batchDetailsId) {
        return entity(amazonClient.fetchAudioReviewersTasks(userId, batchDetailsId));
    }

    @GetMapping({"voices/expert/audio"})
    public ResponseEntity<VoicesToReviewDto> fetchAudioExpertReviewersTasks(@RequestParam Long userId,
                                                                            @RequestParam(required = false) Long batchDetailsId) {
        return entity(amazonClient.fetchAudioExpertReviewersTasks(userId, batchDetailsId));
    }

    @GetMapping({"/fetch/voice"})
    public List<VoiceEntity> allVoiceRecordings(@RequestParam(defaultValue = "0") int pageNo, int size) {
        return voiceSvc.getVoiceByPage(pageNo, size);
    }

    @GetMapping({"/all/voice"})
    public List<VoiceEntity> totalVoiceRecordings() {
        return voiceRepo.findAll();
    }

    @PutMapping({"/approve/voice/{voiceId}"})
    public ResponseEntity<ResponseMessage> approveVoiceRecording(@PathVariable Long voiceId) {
        return entity(voiceSvc.approveVoiceRecording(voiceId));
    }

    @PutMapping({"/reject/voice/{voiceId}"})
    public ResponseEntity<ResponseMessage> rejectVoiceRecording(@RequestParam(name = "rejectionReason", required = false) String rejectionReason,
                                                                @PathVariable Long voiceId) {
        return entity(voiceSvc.rejectVoiceRecording(voiceId, rejectionReason));
    }

    @PutMapping("/voice/expert/review/{voiceId}")
    public ResponseEntity<ResponseMessage> expertReview(@PathVariable Long voiceId,
                                                        @RequestParam boolean approve,
                                                        @RequestParam(required = false) String rejectionReason) {
        if (approve)
            return entity(voiceSvc.expertApprovedVoiceRecording(voiceId));
        else
            return entity(voiceSvc.expertRejectVoiceRecording(voiceId, rejectionReason));
    }

    @PostMapping("/storage/uploadFile/{translatedSentenceId}")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestPart("file") MultipartFile file,
                                                      @PathVariable Long translatedSentenceId,
                                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) throws Exception {
        return amazonClient.uploadFile(file, translatedSentenceId, null, authorizationHeader);
    }

    @PutMapping({"/storage/updateFile/{voiceId}"})
    public ResponseEntity<ResponseMessage> updateFile(@RequestPart("file") MultipartFile file,
                                                      @PathVariable Long voiceId,
                                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) throws Exception {
        return amazonClient.updateFile(voiceId, file, authorizationHeader);
    }

    @DeleteMapping({"/storage/deleteFile/{id}"})
    public String deleteFile(@PathVariable Long id) {
        return amazonClient.deleteFileFromS3Bucket(id, true);
    }
}
