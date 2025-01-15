package kmusau.translator.service;

import java.util.*;
import java.util.stream.Collectors;

import kmusau.translator.DTOs.translatedSentencesDTOs.SentenceToRecordDto;
import kmusau.translator.DTOs.translatedSentencesDTOs.TranslatedSentenceItemDto;
import kmusau.translator.entity.*;
import kmusau.translator.enums.BatchStatus;
import kmusau.translator.repository.*;
import kmusau.translator.response.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import kmusau.translator.enums.StatusTypes;
import kmusau.translator.util.JwtUtil;

@Service
public class VoiceService {
	
	@Autowired
	VoiceRepository voiceRepo;

	@Autowired
	TranslatedSentenceRepository translatedSentenceRepo;

	@Autowired
	BatchDetailsRepository batchDetailsRepo;

	@Autowired
	BatchDetailsStatsRepository batchDetailsStatsRepository;

	@Autowired
	TranslatedSentenceLogsRepo translatedSentenceLogsRepo;
	
	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private AmazonClient amazonClient;
	
	Logger logger = LoggerFactory.getLogger(VoiceService.class);
	
	private static String UPLOADED_FOLDER = "/home/kelvin/eclipse-workspace/hackerrank/";

	
	public List<VoiceEntity> getVoiceByPage(int pageNo, int size) {
		Pageable paging = PageRequest.of(pageNo, size);
		Page<VoiceEntity> PagedResult = voiceRepo.findAll(paging);
		
		if(PagedResult.hasContent()) {
			return PagedResult.getContent();
		} else
		return new ArrayList<VoiceEntity>();
	}
	
	public List<VoiceEntity> getVoiceByStatus(StatusTypes status) {
		return voiceRepo.findByStatus(status);
	}
	
	    
	 //Method to be called on post mapping of create voice
	 public VoiceEntity recordVoice(@RequestHeader (HttpHeaders.AUTHORIZATION) String authorizationHeader, VoiceEntity voice) {
		 
		String username = null;
		String jwt = null; 
			
			
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			jwt = authorizationHeader.substring(7);
			username = jwtUtil.extractUsername(jwt);
		}
		 
		 if(voice.getDateCreated() == null) {
			 voice.setDateCreated(new Date());
			}
			if(voice.getDateModified() == null) {
				voice.setDateModified(new Date());
			}
			if(voice.getStatus() == null) {
				voice.setStatus(StatusTypes.unreviewed);
			}

	        
	        return voiceRepo.save(voice);
	 }
	 
	 public VoiceEntity getSingleVoiceRecord(Long voice_id) {
		 return voiceRepo.findById(voice_id).get();
	 }
	 
	 
//	 public void deleteVoiceRecord(Long voice_id) {
//		 VoiceEntity voice = getSingleVoiceRecord(voice_id);
//		 String fileName = voice.getFilepath().toString();
//		 try {
//	            Files.delete(Paths.get(fileName));
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
//		 voiceRepo.deleteById(voice_id);
//	 }
	 
	 public String recordVoiceTest(@RequestHeader (HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestParam("file") MultipartFile file, 
			 VoiceEntity voice) {
			
			String username = null;
			String jwt = null; 			
		
				
			if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
				jwt = authorizationHeader.substring(7);
				username = jwtUtil.extractUsername(jwt);
			}
			 
			 if(voice.getDateCreated() == null) {
				 voice.setDateCreated(new Date());
				}
				if(voice.getDateModified() == null) {
					voice.setDateModified(new Date());
				}
				if(voice.getStatus() == null) {
					voice.setStatus(StatusTypes.unreviewed);
				}

				
				
				voiceRepo.save(voice);
		
			return "Audio Uploaded successfully";
	 }
	 
	 public VoiceEntity updateStatus(VoiceEntity voiceStatus, Long voice_id) {

			VoiceEntity voice = voiceRepo.findById(voice_id).get();
			
			if(Objects.nonNull(voiceStatus.getStatus())) {
				voice.setStatus(voiceStatus.getStatus());
			}
			
			voice.setDateModified(new Date());
			
			VoiceEntity updatedVoice= voiceRepo.save(voice);
			
			return updatedVoice;
	 }

	//Fetch recorder's pending tasks
	public ResponseEntity<SentenceToRecordDto> recorderAssignedTasks(Long recorderId, BatchStatus batchStatus, Long batchDetailsId) {
		List<BatchDetailsEntity> batchDetails;
		if (batchDetailsId != null) {
			batchDetails = batchDetailsRepo.findByRecordedByIdAndBatchDetailsId(recorderId, batchDetailsId);
		} else {
			batchDetails = batchDetailsRepo.findByRecordedByIdAndBatchStatus(recorderId, batchStatus);;
		}

		Long batchDetailId = null;
		List<TranslatedSentenceItemDto> unrecordedVoiceDto = new ArrayList<>();
		List<TranslatedSentenceItemDto> recordedVoiceTasksDto = new ArrayList<>();
		String language = null;


		if (!batchDetails.isEmpty()) {
			for (BatchDetailsEntity aBatchDetail : batchDetails) {
				batchDetailId = aBatchDetail.getBatchDetailsId();
				language = aBatchDetail.getLanguage().getName();
				List<TranslatedSentenceEntity> unrecordedVoiceTasks = translatedSentenceRepo.findUnrecordedVoiceTasks(batchDetailId);
				unrecordedVoiceDto = unrecordedVoiceTasks.stream()
						.map(entity -> TranslatedSentenceItemDto.entityToDto(entity, null, null))
						.collect(Collectors.toList());

				List<VoiceEntity> recordedVoiceTasks = voiceRepo.findAllByTranslatedSentenceBatchDetails_BatchDetailsId(batchDetailId);
				recordedVoiceTasksDto = recordedVoiceTasks.stream()
						.map(entity -> {
							Boolean isAccepted;
							if (entity.getStatus() == StatusTypes.unreviewed)
								isAccepted = null;
							else {
								isAccepted = entity.getStatus() == StatusTypes.approved;
							}

							String presignedUrl = amazonClient.generatePresignedUrl(entity.getFileUrl());
							entity.setPresignedUrl(presignedUrl);
							return TranslatedSentenceItemDto.voiceEntityToDto(entity, null, isAccepted);
						})
						.collect(Collectors.toList());

				if (!unrecordedVoiceTasks.isEmpty())
					break;
			}
		}

		SentenceToRecordDto sentenceToRecordDto = new SentenceToRecordDto();
		sentenceToRecordDto.setBatchDetailsId(batchDetailId);
		sentenceToRecordDto.setLanguage(language);
		sentenceToRecordDto.setRecordedSentences(recordedVoiceTasksDto);
		sentenceToRecordDto.setUnrecordedSentences(unrecordedVoiceDto);

		return ResponseEntity.ok(sentenceToRecordDto);
	}

	public ResponseEntity<ResponseMessage> approveVoiceRecording(Long voiceId) {
		VoiceEntity voiceEntity = voiceRepo.findById(voiceId).get();
		voiceEntity.setStatus(StatusTypes.approved);
		VoiceEntity updatedVoiceEntity = voiceRepo.save(voiceEntity);
		Long batchDetailsId = voiceEntity.getTranslatedSentence().getBatchDetailsId();
		Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(batchDetailsId);
		if (optionalBatchDetails.isPresent()){
			BatchDetailsEntity batchDetails = optionalBatchDetails.get();
			if (batchDetails.getBatchStatus() == BatchStatus.assignedAudioVerifier){ //Update user stats
				Optional<BatchDetailsStatsEntity> optionalUserStats = batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetails.getBatchDetailsId());
				if (optionalUserStats.isPresent()){
					BatchDetailsStatsEntity userStats = optionalUserStats.get();
					if ((userStats.getAudiosApproved() + userStats.getAudiosRejected()) < userStats.getAudiosRecorded()){
						int audiosApproved = userStats.getAudiosApproved() + 1;
						userStats.setAudiosApproved(audiosApproved);
						batchDetailsStatsRepository.save(userStats);

						TranslatedSentenceLogsEntity translatedSentenceLogs = getTranslatedSentenceLogsEntity(updatedVoiceEntity.getTranslatedSentence());
						translatedSentenceLogs.setDateAudioModerated(new Date());
						translatedSentenceLogsRepo.save(translatedSentenceLogs);
					}
				}
			}
		}
		return ResponseEntity.ok(new ResponseMessage("Voice recording Approved"));
	}

	public ResponseEntity<ResponseMessage> rejectVoiceRecording(Long voiceId) {
		VoiceEntity voiceEntity = voiceRepo.findById(voiceId).get();
		voiceEntity.setStatus(StatusTypes.rejected);
		VoiceEntity updatedVoiceEntity = voiceRepo.save(voiceEntity);

		Long batchDetailsId = voiceEntity.getTranslatedSentence().getBatchDetailsId();
		Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(batchDetailsId);
		if (optionalBatchDetails.isPresent()){
			BatchDetailsEntity batchDetails = optionalBatchDetails.get();
			if (batchDetails.getBatchStatus() == BatchStatus.assignedAudioVerifier){ //Update user stats
				Optional<BatchDetailsStatsEntity> optionalUserStats = batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetails.getBatchDetailsId());
				if (optionalUserStats.isPresent()){
					BatchDetailsStatsEntity userStats = optionalUserStats.get();
					int audiosRejected = userStats.getAudiosRejected() + 1;
					userStats.setAudiosRejected(audiosRejected);
					batchDetailsStatsRepository.save(userStats);

					TranslatedSentenceLogsEntity translatedSentenceLogs = getTranslatedSentenceLogsEntity(updatedVoiceEntity.getTranslatedSentence());
					translatedSentenceLogs.setDateAudioModerated(new Date());
					translatedSentenceLogsRepo.save(translatedSentenceLogs);
				}
			}
		}
		return ResponseEntity.ok(new ResponseMessage("Voice recording Rejected"));
	}

	public List<TranslatedSentenceEntity> findVoiceTasks(Long batchDetailId){
		return translatedSentenceRepo.findUnrecordedVoiceTasks(batchDetailId);
	}

	private TranslatedSentenceLogsEntity getTranslatedSentenceLogsEntity(TranslatedSentenceEntity translatedSentence) {
		TranslatedSentenceLogsEntity translatedSentenceLogs = translatedSentenceLogsRepo.findByTranslatedSentence(translatedSentence);
		if (translatedSentenceLogs == null){
			translatedSentenceLogs = new TranslatedSentenceLogsEntity();
			translatedSentenceLogs.setTranslatedSentence(translatedSentence);
		}
		return translatedSentenceLogs;
	}
}