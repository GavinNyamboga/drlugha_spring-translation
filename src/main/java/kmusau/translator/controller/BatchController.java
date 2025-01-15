package kmusau.translator.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;
import kmusau.translator.DTOs.batchDTO.BatchDto;
import kmusau.translator.DTOs.batchDetails.AddBatchDetailsDto;
import kmusau.translator.DTOs.batchDetails.BatchDetailsDto;
import kmusau.translator.DTOs.batchDTO.BatchResponseDto;
import kmusau.translator.DTOs.batchDTO.CreatePrefixedBatchDto;
import kmusau.translator.DTOs.sentenceDTOs.CompletedSentenceItemDto;
import kmusau.translator.DTOs.sentenceDTOs.CompletedSentencesDto;
import kmusau.translator.DTOs.batchDetails.BatchInfoDto;
import kmusau.translator.DTOs.batchDetails.BatchInfoStatsDto;
import kmusau.translator.DTOs.sentenceDTOs.SentenceItemDto;
import kmusau.translator.entity.BatchDetailsEntity;
import kmusau.translator.entity.VoiceEntity;
import kmusau.translator.enums.StatusTypes;
import kmusau.translator.enums.BatchType;
import kmusau.translator.enums.Task;
import kmusau.translator.repository.*;
import kmusau.translator.response.ResponseMessage;
import kmusau.translator.service.AmazonClient;
import kmusau.translator.service.SentenceBatchService;
import kmusau.translator.service.VoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BatchController {
    @Autowired
    BatchRepository batchRepo;

    @Autowired
    BatchDetailsRepository batchDetailsRepo;

    @Autowired
    TranslatedSentenceRepository translatedSentenceRepository;

    @Autowired
    VoiceRepository voiceRepo; // Autowired VoiceRepository

    @Autowired
    SentenceRepository sentenceRepository; // Autowired SentenceRepository

    @Autowired
    SentenceBatchService batchService;

    @Autowired
    VoiceService voiceService;

    @Autowired
    AmazonClient amazonClient;

    private static final Logger logger = LoggerFactory.getLogger(BatchController.class);

    @GetMapping("/all/batches")
    public ResponseEntity<List<BatchResponseDto>> getAllBatches(@RequestParam(required = false) String batchType) {
        logger.info("Received request to get all batches with type: {}", batchType);
        BatchType batchTypeEnum;
        Optional<BatchType> batchTypeOptional = BatchType.fromName(batchType);
        if (batchTypeOptional.isEmpty()) {
            batchTypeEnum = BatchType.TEXT;
        } else {
            batchTypeEnum = batchTypeOptional.get();
        }
        List<BatchResponseDto> batchResponseDtos = this.batchRepo.findAllByBatchType(batchTypeEnum)
                .stream()
                .map(BatchResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(batchResponseDtos);
    }

    @PostMapping({"/batch"})
    public ResponseEntity<ResponseMessage> addBatch(@RequestBody BatchDto batchDto) {
        return this.batchService.addBatch(batchDto);
    }

    @PutMapping({"/batch"})
    public ResponseEntity editBatch(@RequestBody BatchDto batchDto) {
        return this.batchService.editBatch(batchDto);
    }

    @DeleteMapping({"/batch"})
    public ResponseEntity<ResponseMessage> deleteBatch(Long batchNo) {
        return this.batchService.deleteBatch(batchNo);
    }

    @DeleteMapping({"/batch-details"})
    public ResponseEntity<ResponseMessage> deleteBatchDetails(Long batchDetailsId) {
        return this.batchService.deleteBatchDetails(batchDetailsId);
    }

    @GetMapping({"/all/batch-details"})
    public List<BatchDetailsEntity> getAllBatchDetails() {
        return this.batchDetailsRepo.findAll();
    }

    @GetMapping({"/batch-details/{batchId}"})
    public List<BatchDetailsDto> getBatchDetailsByBatch(@PathVariable Long batchId) {
        return this.batchService.getBatchDetailsByBatch(batchId);
    }

    @PostMapping({"/add/batch-details/{batchNo}"})
    public ResponseEntity addBatchDetails(@RequestBody AddBatchDetailsDto batchDetailsDto, @PathVariable Long batchNo) {
        return this.batchService.addBatchDetails(batchDetailsDto, batchNo);
    }

    @PutMapping({"/"})
    public BatchDetailsEntity editBatchDetailsStatus(@RequestBody BatchDetailsEntity batchDetails, @PathVariable Long batchDetailsId) {
        return this.batchService.editBatchDetailsStatus(batchDetails, batchDetailsId);
    }

    @PutMapping({"/assign/text-verifier/{batchDetailsId}"})
    public BatchDetailsEntity assignTextVerifier(@RequestBody BatchDetailsEntity batchDetails, @PathVariable Long batchDetailsId) {
        return this.batchService.assignTextVerifier(batchDetails, batchDetailsId);
    }

    @PutMapping({"/assign/second-reviewer/{batchDetailsId}"})
    public BatchDetailsEntity assignExpertReviewer(@RequestBody BatchDetailsEntity batchDetails, @PathVariable Long batchDetailsId) {
        return this.batchService.assignExpertReviewer(batchDetails, batchDetailsId);
    }

    @PostMapping("/create-prefixed-batch")
    public ResponseEntity<ResponseMessage> createPrefixedBatch(@RequestBody CreatePrefixedBatchDto batchDto) {
        // Log the batch details
        logger.info("Creating batch with description: {}", batchDto.getDescription());

        // Call the service method and return its response
        return batchService.createPrefixedBatch(batchDto);
    }

    @PutMapping({"/assign/recorder/{batchDetailsId}"})
    public ResponseEntity assignRecorder(@RequestBody BatchDetailsEntity batchDetails, @PathVariable Long batchDetailsId) {
        return this.batchService.assignRecorder(batchDetails, batchDetailsId);
    }

    @PutMapping({"/assign/audio-verifier/{batchDetailsId}"})
    public ResponseEntity assignAudioVerifier(@RequestBody BatchDetailsEntity batchDetails, @PathVariable Long batchDetailsId) {
        return this.batchService.assignAudioVerifier(batchDetails, batchDetailsId);
    }

    @PutMapping({"/batch-status/translated/{batchDetailsId}"})
    public ResponseEntity<ResponseMessage> batchStatusTranslated(@PathVariable Long batchDetailsId) {
        return this.batchService.markTranslationAsComplete(batchDetailsId);
    }

    @PutMapping({"/batch-status/textVerified/{batchDetailsId}"})
    public ResponseEntity<ResponseMessage> textVerified(@PathVariable Long batchDetailsId) {
        return this.batchService.markModerationAsComplete(batchDetailsId);
    }

    @GetMapping({"/affected-batch-details"})
    public ResponseEntity getAffectedBatchDetails() {
        return this.batchService.getAffectedBatcheDetails();
    }

    @PutMapping({"/batch-status/secondVerification/{batchDetailsId}"})
    public ResponseEntity<ResponseMessage> markSecondVerification(@PathVariable Long batchDetailsId) {
        return this.batchService.markExpertVerificationAsComplete(batchDetailsId);
    }

    @PutMapping({"/batch-status/recorded/{batchDetailsId}"})
    public ResponseEntity<ResponseMessage> batchStatusRecorded(@PathVariable Long batchDetailsId) {
        return this.batchService.markBatchAsRecorded(batchDetailsId);
    }

    @PutMapping({"/batch-status/audioVerified/{batchDetailsId}"})
    public ResponseEntity<ResponseMessage> batchStatusAudioVerified(@PathVariable Long batchDetailsId) {
        return this.batchService.markAudioReviewAsComplete(batchDetailsId);
    }

    @GetMapping({"/user-batch-details"})
    public BatchInfoDto getBatchDetailsByTask(@RequestParam Long userId, @RequestParam(defaultValue = "0") Integer task) {
        if (task.intValue() >= (Task.values()).length)
            task = Integer.valueOf(0);
        return this.batchService.getBatchDetailsByTask(userId, Task.values()[task.intValue()]);
    }

    @GetMapping({"batch-details/completed-sentences"})
    public ResponseEntity getCompletedSentencesPerBatchDetails(Long batchDetailsId) {
        return this.batchService.getCompletedSentencesPerBatchDetails(batchDetailsId);
    }

    @GetMapping({"batch-details/stats"})
    public ResponseEntity<List<BatchInfoStatsDto>> getCompletedSentencesPerBatchDetails() {
        return this.batchService.getBatchStats();
}

        @GetMapping("/voice/download")
    public ResponseEntity<byte[]> downloadVoiceFiles(@RequestParam Long batchDetailsId) {
        if (batchDetailsId == null) {
            return ResponseEntity.badRequest().body("Please provide batch details id".getBytes());
        }

        ResponseEntity<byte[]> completedSentencesResponse = getCompletedSentences(batchDetailsId);
        if (completedSentencesResponse.getStatusCode() != HttpStatus.OK) {
            return completedSentencesResponse;
        }

        return completedSentencesResponse;
    }

    

    @PostMapping("/updateAudioUrls")
    public ResponseEntity<String> updateAudioUrls(@RequestBody List<Long> batchDetailsIds) {
        try {
            amazonClient.generateAndHandleUploadUrls(batchDetailsIds);
            return ResponseEntity.ok("Upload URLs generated successfully for batchDetailsIds: " + batchDetailsIds);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error generating upload URLs: " + e.getMessage());
        }
    }


    private ResponseEntity<byte[]> getCompletedSentences(Long batchDetailsId) {
        // Assuming this list is obtained from your repository
        List<SentenceItemDto> sentences = batchDetailsRepo.getAllSentencesInBatchDetails(batchDetailsId);

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
            e.printStackTrace();
            String errorMessage = "Error creating ZIP file for batchDetailsId: " + batchDetailsId + ". Please try again later.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(errorMessage.getBytes());
        }
    }


    


    @GetMapping({"batch-details/expert-reviewed"})
    public ResponseEntity getSentencesAfterExpertReview(Long batchDetailsId) {
        return this.batchService.getTranslatedSentences(batchDetailsId);
    }

    @GetMapping({"expert-reviewed-sentences"})
    public ResponseEntity getExpertReviewedSentences(Long languageId) {
        return this.batchService.getExpertReviewedSentences(languageId);
    }

    @GetMapping({"audio-batches/populate"})
    public ResponseEntity<ResponseMessage> populateAudioBatchesFromS3(String name, Long languageId) {
        return this.amazonClient.populateAudioBatchesFromS3(name, languageId);
    }
}
