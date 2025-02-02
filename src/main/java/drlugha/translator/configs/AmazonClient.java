package drlugha.translator.configs;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.shared.enums.StatusTypes;
import drlugha.translator.system.batch.enums.BatchStatus;
import drlugha.translator.system.batch.enums.BatchType;
import drlugha.translator.system.batch.model.BatchDetailsEntity;
import drlugha.translator.system.batch.model.BatchDetailsStatsEntity;
import drlugha.translator.system.batch.model.BatchEntity;
import drlugha.translator.system.batch.repository.BatchDetailsRepository;
import drlugha.translator.system.batch.repository.BatchDetailsStatsRepository;
import drlugha.translator.system.batch.repository.BatchRepository;
import drlugha.translator.system.language.model.Language;
import drlugha.translator.system.language.repository.LanguageRepository;
import drlugha.translator.system.sentence.dto.TranslatedSentenceItemDto;
import drlugha.translator.system.sentence.dto.VoicesToReviewDto;
import drlugha.translator.system.sentence.model.Sentence;
import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.system.sentence.repository.SentenceRepository;
import drlugha.translator.system.sentence.repository.TranslatedSentenceRepository;
import drlugha.translator.system.user.model.User;
import drlugha.translator.system.user.repository.UserRepository;
import drlugha.translator.system.voice.model.PopulateAudioIndexEntity;
import drlugha.translator.system.voice.model.VoiceEntity;
import drlugha.translator.system.voice.repository.PopulateAudioIndexRepository;
import drlugha.translator.system.voice.repository.VoiceRepository;
import drlugha.translator.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AmazonClient {

    private final Logger logger;

    private final VoiceRepository voiceRepo;

    private final TranslatedSentenceRepository translatedRepo;

    private final BatchDetailsRepository batchDetailsRepository;

    private final UserRepository userRepository;

    private final BatchRepository batchRepository;

    private final BatchDetailsStatsRepository batchDetailsStatsRepository;

    private final SentenceRepository sentenceRepository;

    private final LanguageRepository languageRepository;

    private final PopulateAudioIndexRepository populateAudioIndexRepository;

    private final JwtUtil jwtUtil;

    private AmazonS3 s3client;

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;
    @Value("${amazonProperties.bucketName}")
    private String bucketName;
    @Value("${amazonProperties.accessKey}")
    private String accessKey;
    @Value("${amazonProperties.secretKey}")
    private String secretKey;

    @PostConstruct
    private void initializeAmazon() {
        BasicAWSCredentials creds = new BasicAWSCredentials(this.accessKey, this.secretKey);

        this.s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .withRegion(Regions.US_EAST_1)
                .build();


//        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
//        this.s3client = new AmazonS3Client(credentials, Region.getRegion(Regions.EU_CENTRAL_1));
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getName());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + multiPart.getName().replace(" ", "_");
    }

    private void uploadFileTos3bucket(String fileName, InputStream inputStream) {
        s3client.putObject(bucketName, fileName, inputStream, null);
    }


    public ResponseEntity<ResponseMessage> uploadFile(MultipartFile multipartFile, Long translatedSentenceId, Long voiceId,
                                                      Long userId, String authorizationHeader) throws Exception {
        // Check if the file is empty (0 KB)
        if (multipartFile.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessage("File is empty and cannot be uploaded."));
        }

        TranslatedSentenceEntity translatedSentenceEntity = translatedRepo.findById(translatedSentenceId)
                .orElseThrow(() -> new Exception("TranslatedSentence not found"));

        String currentUsername = jwtUtil.extractUsername(authorizationHeader);
        logger.info("Current username: {}", currentUsername);

        User userEntity = userRepository.findByUsername(currentUsername).orElseThrow(() -> new Exception("User not found"));

        VoiceEntity voice = new VoiceEntity();
        String fileUrl;

        String fileName = new Date().getTime() + "_" + multipartFile.getOriginalFilename();
        String storeFileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
        uploadFileTos3bucket(fileName, multipartFile.getInputStream());

        fileUrl = generatePresignedUrl(fileName);
        translatedSentenceEntity.setRecordedStatus(StatusTypes.recorded);
        voice.setTranslatedSentenceId(translatedSentenceId);
        voice.setTranslatedSentence(translatedSentenceEntity);
        voice.setBatchDetailsId(translatedSentenceEntity.getBatchDetailsId());
        voice.setFileUrl(storeFileUrl);
        voice.setDateCreated(new Date());
        voice.setDateModified(new Date());
        voice.setStatus(StatusTypes.unreviewed);
        voice.setPresignedUrl("");
        if (voiceId != null) {
            voice.setVoiceId(voiceId);
        }
        voice.setUser(userEntity);
        voiceRepo.save(voice);

        Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepository.findById(translatedSentenceEntity.getBatchDetailsId());
        if (optionalBatchDetails.isPresent()) {
            BatchDetailsEntity batchDetails = optionalBatchDetails.get();
            if (batchDetails.getBatchStatus() == BatchStatus.ASSIGNED_RECORDER) { // Update user stats
                Optional<BatchDetailsStatsEntity> optionalUserStats = batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetails.getBatchDetailsId());
                if (optionalUserStats.isPresent()) {
                    BatchDetailsStatsEntity userStats = optionalUserStats.get();
                    int audiosRecorded = userStats.getAudiosRecorded() + 1;
                    userStats.setAudiosRecorded(audiosRecorded);
                    batchDetailsStatsRepository.save(userStats);
                }
            }
        }

        return ResponseEntity.ok(new ResponseMessage(fileUrl));
    }

    public ResponseEntity<ResponseMessage> updateFile(Long voiceId, MultipartFile multipartFile, String authorizationHeader) throws Exception {
        if (voiceId == null) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Please provide voice id"));
        }
        Optional<VoiceEntity> optionalVoice = voiceRepo.findById(voiceId);
        if (optionalVoice.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Voice does not exist"));
        }
        VoiceEntity voiceEntity = optionalVoice.get();
        Long userId = voiceEntity.getUser().getUserId();  // Get the user ID from the VoiceEntity
        return uploadFile(multipartFile, voiceEntity.getTranslatedSentenceId(), voiceEntity.getVoiceId(), userId, authorizationHeader);
    }

    public String deleteFileFromS3Bucket(Long id, boolean deleteVoiceFromDb) {
        VoiceEntity voiceEntity = voiceRepo.findById(id).get();
        String fileUrl = voiceEntity.getFileUrl();
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        logger.info(fileName);
        s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));

        if (deleteVoiceFromDb) {
            voiceRepo.deleteById(id);
        }
        return "Successfully deleted";
    }

    public String getSingleAudio(Long voiceId) {
//        System.setProperty(SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 1); // Generatecd URL will be valid for 24 hours

        VoiceEntity voiceEntity = voiceRepo.findById(voiceId).get();
        URL fileUrl;
        String storedFileUrl = voiceEntity.getFileUrl();
        String fileName = storedFileUrl.substring(storedFileUrl.lastIndexOf("/") + 1);
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, fileName)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(calendar.getTime());

        fileUrl = this.s3client.generatePresignedUrl(generatePresignedUrlRequest);
        return fileUrl.toString();
//        return fileUrl;
    }

    public String generatePresignedUrl(String fileUrl) {
        if (Strings.isBlank(fileUrl)) {
            return null;
        }
        String[] fileUrlSplit = fileUrl.split(bucketName + "/");
        String fileName = fileUrlSplit[fileUrlSplit.length - 1];
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, fileName)
                        .withMethod(HttpMethod.GET);

        return this.s3client.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }


    public VoicesToReviewDto fetchAudioReviewersTasks(Long reviewerId, Long batchDetailsId) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Fetching audio reviewer's tasks. Reviewer ID: {}, Batch Details ID: {}", reviewerId, batchDetailsId);

        List<BatchDetailsEntity> batchesDetailsToVerify;

        if (batchDetailsId != null) {
            logger.info("Batch Details ID is provided. Fetching specific batch details...");
            batchesDetailsToVerify = batchDetailsRepository.findAllByBatchDetailsIdAndAudioVerifiedById(batchDetailsId, reviewerId);
        } else {
            logger.info("No specific Batch Details ID provided. Fetching all batches assigned to the reviewer...");
            batchesDetailsToVerify = batchDetailsRepository.findAllByAudioVerifiedById(reviewerId);
        }

        if (batchesDetailsToVerify.isEmpty()) {
            logger.warn("No batches found for Reviewer ID: {}", reviewerId);
        } else {
            logger.info("Found {} batch(es) for Reviewer ID: {}", batchesDetailsToVerify.size(), reviewerId);
        }

        String language = null;
        List<TranslatedSentenceItemDto> unreviewedAudios = new ArrayList<>();
        List<TranslatedSentenceItemDto> reviewedAudios = new ArrayList<>();

        for (BatchDetailsEntity batchDetails : batchesDetailsToVerify) {
            language = batchDetails.getLanguage().getName();
            batchDetailsId = batchDetails.getBatchDetailsId();

            logger.info("Processing Batch Details ID: {}, Language: {}", batchDetailsId, language);

            List<VoiceEntity> allVoiceEntities = voiceRepo.findAllAudios(reviewerId, batchDetailsId);

            //filter these by reviewed and unreviewed
            List<VoiceEntity> unreviewed = allVoiceEntities.stream()
                    .filter(v -> v.getStatus() == StatusTypes.unreviewed)
                    .collect(Collectors.toList());

            List<VoiceEntity> reviewed = allVoiceEntities.stream()
                    .filter(v -> v.getStatus() == StatusTypes.approved || v.getStatus() == StatusTypes.rejected)
                    .collect(Collectors.toList());

            // Fetch unreviewed audios
            //List<VoiceEntity> unreviewedEntities = voiceRepo.findUnreviewedAudios(reviewerId, batchDetailsId);
            logger.info("Found {} unreviewed audio(s) for Batch Details ID: {}", unreviewed.size(), batchDetailsId);

            //One translated sentence can have multiple audios
            Map<Long, List<VoiceEntity>> unreviewedMap = unreviewed.stream().collect(Collectors.groupingBy(VoiceEntity::getTranslatedSentenceId));
            Map<Long, List<VoiceEntity>> reviewedMap = reviewed.stream().collect(Collectors.groupingBy(VoiceEntity::getTranslatedSentenceId));

            for (Map.Entry<Long, List<VoiceEntity>> entry : unreviewedMap.entrySet()) {
                Long translatedSentenceId = entry.getKey();

                List<VoiceEntity> voiceEntities = entry.getValue();
                TranslatedSentenceEntity translatedSentence = voiceEntities.get(0).getTranslatedSentence();
                if (translatedSentence == null) {
                    translatedSentence = translatedRepo.findById(translatedSentenceId).get();
                }

                TranslatedSentenceItemDto translatedSentenceItemDto = TranslatedSentenceItemDto.entityToDto(translatedSentence, null, null);

                for (VoiceEntity voiceEntity : voiceEntities) {
                    String presignedUrl = generatePresignedUrl(voiceEntity.getFileUrl());
                    voiceEntity.setPresignedUrl(presignedUrl);
                    logger.debug("Generated presigned URL for unreviewed audio: {}", presignedUrl);

                    TranslatedSentenceItemDto.AudioDTO audioDTO = new TranslatedSentenceItemDto.AudioDTO();
                    audioDTO.setAudioLink(presignedUrl);
                    audioDTO.setVoiceId(voiceEntity.getVoiceId());

                    if (voiceEntity.getUser() != null) {
                        audioDTO.setRecordedBy(voiceEntity.getUser().getUsername());
                    }

                    translatedSentenceItemDto.getAudioList().add(audioDTO);
                }
                unreviewedAudios.add(translatedSentenceItemDto);
            }

           /* unreviewedAudios = unreviewed.stream()
                    .map(entity -> {
                        String presignedUrl = generatePresignedUrl(entity.getFileUrl());
                        entity.setPresignedUrl(presignedUrl);
                        logger.debug("Generated presigned URL for unreviewed audio: {}", presignedUrl);
                        return TranslatedSentenceItemDto.voiceEntityToDto(entity, null, null);
                    })
                    .collect(Collectors.toList());*/

            // Fetch reviewed audios
            //List<VoiceEntity> reviewedEntities = voiceRepo.findReviewedAudios(reviewerId, batchDetailsId);
            logger.info("Found {} reviewed audio(s) for Batch Details ID: {}", reviewed.size(), batchDetailsId);

            for (Map.Entry<Long, List<VoiceEntity>> entry : reviewedMap.entrySet()) {
                Long translatedSentenceId = entry.getKey();

                List<VoiceEntity> voiceEntities = entry.getValue();
                TranslatedSentenceEntity translatedSentence = voiceEntities.get(0).getTranslatedSentence();
                if (translatedSentence == null) {
                    translatedSentence = translatedRepo.findById(translatedSentenceId).get();
                }

                TranslatedSentenceItemDto translatedSentenceItemDto = TranslatedSentenceItemDto.entityToDto(translatedSentence, null, null);

                for (VoiceEntity voiceEntity : voiceEntities) {

                    Boolean isAccepted = (voiceEntity.getStatus() == StatusTypes.unreviewed) ? null : (voiceEntity.getStatus() == StatusTypes.approved);

                    String presignedUrl = generatePresignedUrl(voiceEntity.getFileUrl());
                    voiceEntity.setPresignedUrl(presignedUrl);
                    logger.debug("Generated presigned URL for reviewed audio: {}, Accepted: {}", presignedUrl, isAccepted);

                    TranslatedSentenceItemDto.AudioDTO audioDTO = new TranslatedSentenceItemDto.AudioDTO();
                    audioDTO.setAudioLink(presignedUrl);
                    audioDTO.setVoiceId(voiceEntity.getVoiceId());
                    audioDTO.setAccepted(isAccepted);

                    if (voiceEntity.getUser() != null) {
                        audioDTO.setRecordedBy(voiceEntity.getUser().getUsername());
                    }

                    translatedSentenceItemDto.getAudioList().add(audioDTO);
                }
                reviewedAudios.add(translatedSentenceItemDto);
            }

         /*   reviewedAudios = reviewed.stream()
                    .map(entity -> {
                        Boolean isAccepted = (entity.getStatus() == StatusTypes.unreviewed) ? null
                                : (entity.getStatus() == StatusTypes.approved);
                        String presignedUrl = generatePresignedUrl(entity.getFileUrl());
                        entity.setPresignedUrl(presignedUrl);
                        logger.debug("Generated presigned URL for reviewed audio: {}, Accepted: {}", presignedUrl, isAccepted);
                        return TranslatedSentenceItemDto.voiceEntityToDto(entity, null, isAccepted);
                    })
                    .collect(Collectors.toList());*/
        }

        VoicesToReviewDto voicesToReviewDto = new VoicesToReviewDto();
        voicesToReviewDto.setBatchDetailsId(batchDetailsId);
        voicesToReviewDto.setLanguage(language);
        voicesToReviewDto.setReviewedAudios(reviewedAudios);
        voicesToReviewDto.setUnreviewedAudios(unreviewedAudios);

        logger.info("Returning VoicesToReviewDto with Batch Details ID: {}, Language: {}, {} unreviewed audio(s), {} reviewed audio(s)",
                batchDetailsId, language, unreviewedAudios.size(), reviewedAudios.size());

        return voicesToReviewDto;
    }


    @Transactional
    public void updateAudioUrlInDatabase(Long batchDetailsId, String oldUrl, String newUrl) {
        logger.info("Fetching sentence with batchDetailsId: {} and oldUrl: {}", batchDetailsId, oldUrl);
        Sentence sentence = batchDetailsRepository.findSentenceByAudioUrl(batchDetailsId, oldUrl);
        if (sentence != null) {
            logger.info("Updating audio URL for sentenceId: {}", sentence.getSentenceId());
            sentence.setAudioLink(newUrl); // Update the audioLink field
            sentenceRepository.save(sentence);
            logger.info("Updated audio URL in database: {}", newUrl);
        } else {
            logger.warn("Sentence not found for batchDetailsId: {} and oldUrl: {}", batchDetailsId, oldUrl);
        }
    }


    public void generateAndHandleUploadUrls(List<Long> batchDetailsIds) throws IOException {
        for (Long batchDetailsId : batchDetailsIds) {
            logger.info("Processing batchDetailsId: {}", batchDetailsId);
            List<Sentence> sentences = batchDetailsRepository.findAllSentencesByBatchDetailsId(batchDetailsId);

            logger.info("Found {} sentences for batchDetailsId: {}", sentences.size(), batchDetailsId);

            for (Sentence sentence : sentences) {
                String oldUrl = sentence.getAudioUrl(); // Assuming the audio URL is fetched correctly
                if (oldUrl != null && !oldUrl.isEmpty()) {
                    logger.info("Processing old URL: {} for sentenceId: {}", oldUrl, sentence.getSentenceId());

                    // Extract the filename from the old URL
                    String objectKey = extractFilenameFromUrl(oldUrl);

                    // Download the file from the old URL
                    try {
                        byte[] audioData = downloadFileFromURL(oldUrl);
                        if (audioData != null) {
                            // Upload the file to S3
                            String newUrl = uploadFileToS3(bucketName, objectKey, audioData);
                            logger.info("Uploaded file to S3: {}", newUrl);

                            // Update the audio URL in the database
                            updateAudioUrlInDatabase(batchDetailsId, oldUrl, newUrl);
                        } else {
                            logger.warn("Failed to download audio data from old URL: {}", oldUrl);
                        }
                    } catch (IOException e) {
                        logger.error("Error generating upload URLs: " + e.getMessage());
                    }
                } else {
                    logger.warn("Old URL is null or empty for sentenceId: {}", sentence.getSentenceId());
                }
            }
        }
    }

    public byte[] downloadFileFromURL(String fileUrl) throws IOException {
        logger.info("Attempting to download file from URL: " + fileUrl);

        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        logger.info("Response Code: " + responseCode);
        if (responseCode == 200) {
            logger.info("Successfully connected to the URL.");
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] data = inputStream.readAllBytes();
                logger.info("Downloaded " + data.length + " bytes.");
                return data;
            }
        } else {
            logger.info("Failed to fetch file content from URL: " + fileUrl + " with response code: " + responseCode);
            throw new IOException("Failed to fetch file content from URL: " + fileUrl + " with response code: " + responseCode);
        }
    }


    private String uploadFileToS3(String bucketName, String objectKey, byte[] content) {
        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(content.length);
            s3client.putObject(bucketName, objectKey, inputStream, metadata);
            String s3Url = s3client.getUrl(bucketName, objectKey).toString();
            logger.info("File uploaded to S3 with URL: {}", s3Url);
            return s3Url;
        } catch (IOException e) {
            logger.error("Failed to upload file to S3", e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    private String extractFilenameFromUrl(String url) {
        try {
            return new URL(url).getPath().substring(url.lastIndexOf('/') + 1);
        } catch (MalformedURLException e) {
            logger.error("Invalid URL format: {}", url, e);
            return null;
        }
    }


    @Transactional
    public ResponseEntity<ResponseMessage> populateAudioBatchesFromS3(String name, Long languageId) {
        String batchPrefix = name;
        Optional<Language> language = languageRepository.findById(languageId);
        String audioBucketName = "audio_files/" + batchPrefix;

        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(audioBucketName);

        ListObjectsV2Result listObjectsResult;
        List<S3ObjectSummary> objectSummaries = new ArrayList<>();

        do {
            listObjectsResult = s3client.listObjectsV2(listObjectsRequest);
            objectSummaries.addAll(listObjectsResult.getObjectSummaries());
            String token = listObjectsResult.getNextContinuationToken();
            listObjectsRequest.setContinuationToken(token);
        } while (listObjectsResult.isTruncated());

        objectSummaries.sort((summary, summary1) -> {
            String[] splitAudioLink = summary.getKey().split("/");
            String fileName = splitAudioLink[splitAudioLink.length - 1];
            String[] splitAudioLink1 = summary1.getKey().split("/");
            String fileName1 = splitAudioLink1[splitAudioLink1.length - 1];

            int fileIndex = Integer.parseInt(fileName.split(".wav")[0]);
            int fileIndex1 = Integer.parseInt(fileName1.split(".wav")[0]);

            return Integer.compare(fileIndex, fileIndex1);
        });

        ArrayList<Sentence> audioSentences = new ArrayList<>();
        Long currentBatchNo = null;

        PopulateAudioIndexEntity populateAudioIndexEntity = populateAudioIndexRepository.getPopulateAudioIndexEntitiesByBucketName(name);
        if (populateAudioIndexEntity == null) {
            populateAudioIndexEntity = new PopulateAudioIndexEntity();
            populateAudioIndexEntity.setLastAudioIndex(0);
            populateAudioIndexEntity.setBucketName(name);
        }
        Long index = populateAudioIndexEntity.getLastAudioIndex();

        for (; index < objectSummaries.size(); index++) {
            if (index % 500 == 0) {
                String batchName = batchPrefix + " (" + (index + 1) + "-" + (index + 500) + ")";
                BatchEntity batchEntity = new BatchEntity(batchName, "DrLugha/" + batchPrefix, "Extracted from DrLugha " + batchPrefix, BatchType.AUDIO, language.get());
                currentBatchNo = batchRepository.save(batchEntity).getBatchNo();
            }
            S3ObjectSummary objectSummary = objectSummaries.get(index.intValue());
            String fileName = objectSummary.getKey();
            String storeFileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
            Sentence audioSentence = new Sentence(currentBatchNo, storeFileUrl);
            audioSentences.add(audioSentence);
        }

        sentenceRepository.saveAll(audioSentences);
        populateAudioIndexEntity.setLastAudioIndex(index);
        populateAudioIndexRepository.save(populateAudioIndexEntity);

        return ResponseEntity.ok(new ResponseMessage("Databases successfully populated"));
    }


}
