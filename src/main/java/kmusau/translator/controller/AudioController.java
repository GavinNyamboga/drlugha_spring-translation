package kmusau.translator.controller;

import java.util.List;
import kmusau.translator.DTOs.translatedSentencesDTOs.SentenceToRecordDto;
import kmusau.translator.DTOs.translatedSentencesDTOs.VoicesToReviewDto;
import kmusau.translator.entity.TranslatedSentenceEntity;
import kmusau.translator.entity.VoiceEntity;
import kmusau.translator.enums.BatchStatus;
import kmusau.translator.enums.StatusTypes;
import kmusau.translator.repository.TranslatedSentenceRepository;
import kmusau.translator.repository.VoiceRepository;
import kmusau.translator.response.ResponseMessage;
import kmusau.translator.service.AmazonClient;
import kmusau.translator.service.VoicePlayerService;
import kmusau.translator.service.VoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AudioController {
    private AmazonClient amazonClient;

    @Autowired
    TranslatedSentenceRepository translatedRepo;

    @Autowired
    VoiceService voiceSvc;

    @Autowired
    VoicePlayerService voicePlayerSvc;

    @Autowired
    VoiceRepository voiceRepo;

    @Autowired
    AudioController(AmazonClient amazonClient) {
        this.amazonClient = amazonClient;
    }

    @PutMapping({"/assign/audiotasks/{recorderId}/{audioReviewerId}"})
    public ResponseMessage assignAudioRecorderAndAudioReviewer(@RequestBody List<TranslatedSentenceEntity> translatedSentences, @PathVariable Long recorderId, @PathVariable Long audioReviewerId) {
        if (recorderId == null || audioReviewerId == null)
            return new ResponseMessage("Recorder Id or Audio-Reviewer Id cannot be empty");
        try {
            translatedSentences.forEach(translatedSentenceEntity -> translatedSentenceEntity.setAssignedRecorderId(recorderId));
            translatedSentences.forEach(translatedSentenceEntity -> translatedSentenceEntity.setAssignedAudioReviewerId(audioReviewerId));
            translatedSentences.forEach(translatedSentenceEntity -> translatedSentenceEntity.setRecordedStatus(StatusTypes.assigned));
            this.translatedRepo.saveAll(translatedSentences);
            return new ResponseMessage("Updated list successfully");
        } catch (Exception e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @GetMapping({"users/audio/assignments"})
    public List<TranslatedSentenceEntity> getAudioAssignments(@RequestParam(defaultValue = "assigned") StatusTypes recordedStatus, @RequestParam Long userId) {
        return this.translatedRepo.findByRecordedStatusAndAssignedRecorderUserId(recordedStatus, userId);
    }

    @GetMapping({"/recorder/tasks"})
    public ResponseEntity<SentenceToRecordDto> usersAudioTasks(@RequestParam(defaultValue = "assignedRecorder") BatchStatus batchStatus, @RequestParam Long recorderId, @RequestParam(required = false) Long batchDetailsId) {
        return this.voiceSvc.recorderAssignedTasks(recorderId, batchStatus, batchDetailsId);
    }

    @GetMapping({"/fetch/voice/{id}"})
    public String getSingleAudio(@PathVariable Long id) {
        return this.amazonClient.getSingleAudio(id);
    }

    @GetMapping({"/approved/voice"})
    public List<VoiceEntity> getVoiceByStatus(@RequestParam(defaultValue = "approved") StatusTypes status) {
        return this.voiceSvc.getVoiceByStatus(status);
    }

    @GetMapping({"/recorder/audiotasks"})
    public List<TranslatedSentenceEntity> getUsersAudioTasks(@RequestParam(defaultValue = "unreviewed") StatusTypes status, @RequestParam Long userId) {
        return this.translatedRepo.findByRecordedStatusAndAssignedRecorderUserId(status, userId);
    }

    @GetMapping({"/reviewer/audio"})
    public ResponseEntity<VoicesToReviewDto> getVoicesToReview(@RequestParam Long userId, @RequestParam(required = false) Long batchDetailsId) {
        return this.amazonClient.fetchAudioReviewersTasks(userId, batchDetailsId);
    }

    @GetMapping({"/fetch/voice"})
    public List<VoiceEntity> allVoiceRecordings(@RequestParam(defaultValue = "0") int pageNo, int size) {
        return this.voiceSvc.getVoiceByPage(pageNo, size);
    }

    @GetMapping({"/all/voice"})
    public List<VoiceEntity> totalVoiceRecordings() {
        return this.voiceRepo.findAll();
    }

    @PutMapping({"/approve/voice/{voiceId}"})
    public ResponseEntity<ResponseMessage> approveVoiceRecording(@PathVariable Long voiceId) {
        return this.voiceSvc.approveVoiceRecording(voiceId);
    }

    @PutMapping({"/reject/voice/{voiceId}"})
    public ResponseEntity<ResponseMessage> rejectVoiceRecording(@PathVariable Long voiceId) {
        return this.voiceSvc.rejectVoiceRecording(voiceId);
    }

        @PostMapping("/uploadFile/{translatedSentenceId}")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestPart("file") MultipartFile file,
                                                      @PathVariable Long translatedSentenceId,
                                                      @RequestParam Long userId) throws Exception {
        return this.amazonClient.uploadFile(file, translatedSentenceId, null, userId);
    }

    @PutMapping({"/storage/updateFile/{voiceId}"})
    public ResponseEntity<ResponseMessage> updateFile(@RequestPart("file") MultipartFile file, @PathVariable Long voiceId) throws Exception {
        return this.amazonClient.updateFile(voiceId, file);
    }

    @DeleteMapping({"/storage/deleteFile/{id}"})
    public String deleteFile(@PathVariable Long id) {
        return this.amazonClient.deleteFileFromS3Bucket(id, true);
    }
}
