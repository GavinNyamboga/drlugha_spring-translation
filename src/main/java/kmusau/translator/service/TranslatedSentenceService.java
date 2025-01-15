package kmusau.translator.service;

import kmusau.translator.DTOs.translatedSentencesDTOs.RejectTranslationDto;
import kmusau.translator.DTOs.translatedSentencesDTOs.TranslateSentenceDto;
import kmusau.translator.DTOs.translatedSentencesDTOs.TranslatedSentencesPerBatchDto;
import kmusau.translator.entity.*;
import kmusau.translator.enums.BatchStatus;
import kmusau.translator.enums.BatchType;
import kmusau.translator.enums.StatusTypes;
import kmusau.translator.repository.*;
import kmusau.translator.response.ResponseMessage;
import kmusau.translator.util.JwtUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
public class TranslatedSentenceService {

	@Autowired
	TranslatedSentenceRepository translatedRepo;

	@Autowired
	BatchDetailsRepository batchDetailsRepo;

	@Autowired
	SentenceRepository sentenceRepo;

	@Autowired
	TranslateAssignmentRepository assignmentRepo;

	@Autowired
	CorrectedSentencesRepository correctedSentencesRepo;

	@Autowired
	ModeratorCommentRepo moderatorCommentRepo;

	@Autowired
	ExpertCommentRepo expertCommentRepo;

	@Autowired
	BatchDetailsStatsRepository batchDetailsStatsRepository;

	@Autowired
	TranslatedSentenceLogsRepo translatedSentenceLogsRepo;
	
	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	Logger logger;

	@Autowired
	AmazonClient amazonClient;

	
	public List<TranslatedSentenceEntity> getTranslatedSentencesByPage(int pageNo, int size) {
		Pageable paging = PageRequest.of(pageNo, size);
		Page<TranslatedSentenceEntity> PagedResult = translatedRepo.findAll(paging);
		
		if(PagedResult.hasContent()) {
			return PagedResult.getContent();
		} else
		return new ArrayList<TranslatedSentenceEntity>();
	}
	
	public List<TranslatedSentenceEntity> getTranslatedByStatus() {
		return translatedRepo.findByReviewStatusAndRecordedStatus();
	}
	
//	public TranslatedSentenceEntity translateSentence(TranslateSentenceDto translateSentenceDto, Long assignmentId) throws Exception{
//
//		if(translateSentenceDto.getTranslatedText() == null || translateSentenceDto.getTranslatedText().equals("")) {
//			throw new Exception("Translated text not captured");
//		} else {
//
//			TranslatedSentenceEntity translatedSentence = new TranslatedSentenceEntity();
//			translatedSentence.setTranslatedText(translateSentenceDto.getTranslatedText());
//
//			AssignedSentencesEntity assignmentEntity = assignmentRepo.findById(assignmentId).get();
//				translatedSentence.setLanguage(assignmentEntity.getTranslateToLanguage());
//				translatedSentence.setAssignedToReview(assignmentEntity.getAssignedToReview().getUserId());
//				translatedSentence.setAssignedTranslator(assignmentEntity.getTranslator().getUserId());
//				translatedSentence.setAssignedSentenceId(assignmentId);
//				assignmentEntity.setTranslationStatus(StatusTypes.completed);
//
//			if(translatedSentence.getDateCreated() == null) {
//				translatedSentence.setDateCreated(new Date());
//			}
//			if(translatedSentence.getDateModified() == null) {
//				translatedSentence.setDateModified(new Date());
//			}
//			if(translatedSentence.getReviewStatus() == null) {
//				translatedSentence.setReviewStatus(StatusTypes.unreviewed);
//			}
//			if (translatedSentence.getRecordedStatus() == null) {
//				translatedSentence.setRecordedStatus(StatusTypes.notRecorded);
//			}
//
//
//			return translatedRepo.save(translatedSentence);
//		}
//
//	}

	@Transactional
	public TranslatedSentenceEntity newtranslateSentence(TranslateSentenceDto translateSentenceDto, Long sentenceId) throws Exception {
		if(translateSentenceDto.getTranslatedText() == null || translateSentenceDto.getTranslatedText().equals("")) {
			throw new Exception("Translated text not captured");
		} else {
			List<TranslatedSentenceEntity> existingTranslations = translatedRepo.findAllBySentenceIdAndBatchDetailsId(sentenceId, translateSentenceDto.getBatchDetailsId());
			TranslatedSentenceEntity translatedSentence;
			if (!existingTranslations.isEmpty())
				translatedSentence = existingTranslations.get(0);
			else
				translatedSentence = new TranslatedSentenceEntity();
			translatedSentence.setTranslatedText(translateSentenceDto.getTranslatedText());

			BatchDetailsEntity batchDetails = batchDetailsRepo.findById(translateSentenceDto.getBatchDetailsId()).get();
			String language = batchDetails.getLanguage().toString();
			long id = batchDetails.getTranslatedBy().getUserId();
			logger.info("" + id);
			logger.info(language);


			translatedSentence.setLanguage(batchDetails.getLanguage());
			translatedSentence.setBatchDetailsId(translateSentenceDto.getBatchDetailsId());
			translatedSentence.setReviewStatus(StatusTypes.unreviewed);
			translatedSentence.setSentenceId(sentenceId);
			if (translatedSentence.getTranslatedSentenceId() == null){ //Update user stats
				Optional<BatchDetailsStatsEntity> optionalUserStats = batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(translateSentenceDto.getBatchDetailsId());
				BatchDetailsStatsEntity userStats;
				userStats = optionalUserStats.orElseGet(BatchDetailsStatsEntity::new);
				int sentencesTranslated = userStats.getSentencesTranslated() + 1;
				userStats.setSentencesTranslated(sentencesTranslated);
				userStats.setBatchDetails(batchDetails);
				batchDetailsStatsRepository.save(userStats);
			}

			return translatedRepo.save(translatedSentence);
		}

	}
	
	public TranslatedSentenceEntity editTranslatedSentence(TranslatedSentenceEntity translatedSentence, Long id) {
		TranslatedSentenceEntity translatedStnc = translatedRepo.findById(id).get();

		if(Objects.nonNull(translatedSentence.getTranslatedText())) {
			translatedStnc.setTranslatedText(translatedSentence.getTranslatedText());
		}

		translatedStnc.setReviewStatus(StatusTypes.unreviewed);
		return translatedRepo.save(translatedStnc);
	}
	
	@Transactional
	public TranslatedSentenceEntity approveTranslatedSentence(Long id) {

		TranslatedSentenceEntity translatedStnc = translatedRepo.findById(id).get();
//		AssignedSentencesEntity assignedSentence = assignmentRepo.findById(id).get();

		translatedStnc.setReviewStatus(StatusTypes.approved);
		if (translatedStnc.getSecondReview() == StatusTypes.rejected)
			translatedStnc.setSecondReview(StatusTypes.unreviewed);
		TranslatedSentenceEntity updatedSentence = translatedRepo.save(translatedStnc);

		Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(updatedSentence.getBatchDetailsId());
		if (optionalBatchDetails.isPresent()){
			BatchDetailsEntity batchDetails = optionalBatchDetails.get();
			if (batchDetails.getBatchStatus() == BatchStatus.assignedTextVerifier){ //Update user stats
				Optional<BatchDetailsStatsEntity> optionalUserStats = batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetails.getBatchDetailsId());
				if (optionalUserStats.isPresent()){
					BatchDetailsStatsEntity userStats = optionalUserStats.get();
					if ((userStats.getSentencesApproved() + userStats.getSentencesRejected()) < userStats.getSentencesTranslated()){
						int sentencesApproved = userStats.getSentencesApproved() + 1;
						userStats.setSentencesApproved(sentencesApproved);
						batchDetailsStatsRepository.save(userStats);

						TranslatedSentenceLogsEntity translatedSentenceLogs = getTranslatedSentenceLogsEntity(updatedSentence);
						translatedSentenceLogs.setDateModerated(new Date());
						translatedSentenceLogsRepo.save(translatedSentenceLogs);
					}
				}
			}
		}


		return updatedSentence;
	}

	@Transactional
	public ResponseEntity<ResponseMessage> rejectTranslatedSentence(RejectTranslationDto rejectTranslationDto, String username) {
		ResponseEntity<ResponseMessage> body = validateDto(rejectTranslationDto);
		if (body != null) return body;

		Optional<TranslatedSentenceEntity> optionalTranslatedSentence = translatedRepo
				.findById(rejectTranslationDto.getTranslatedSentenceId());

		if (optionalTranslatedSentence.isEmpty())
			return ResponseEntity.badRequest().body(new ResponseMessage("Error! The translated sentence you are trying to reject does not exist"));

		TranslatedSentenceEntity translatedStnc = optionalTranslatedSentence.get();
		if (!translatedStnc.getBatchDetails().getTranslationVerifiedBy().getUsername().matches(username)){
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMessage("You are unauthorized to reject this translation"));
		}
		translatedStnc.setReviewStatus(StatusTypes.rejected);

		ModeratorCommentEntity moderatorCommentEntity =
				moderatorCommentRepo.findAllByTranslatedSentence_TranslatedSentenceId(translatedStnc.getTranslatedSentenceId());

		if (moderatorCommentEntity == null)
			moderatorCommentEntity = new ModeratorCommentEntity();

		moderatorCommentEntity.setTranslatedSentence(translatedStnc);
		moderatorCommentEntity.setComment(rejectTranslationDto.getComment());

		TranslatedSentenceEntity updatedSentence = translatedRepo.save(translatedStnc);
		moderatorCommentRepo.save(moderatorCommentEntity);

		Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(translatedStnc.getBatchDetailsId());
		if (optionalBatchDetails.isPresent()){
			BatchDetailsEntity batchDetails = optionalBatchDetails.get();
			if (batchDetails.getBatchStatus() == BatchStatus.assignedTextVerifier){ //Update user stats
				Optional<BatchDetailsStatsEntity> optionalUserStats = batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetails.getBatchDetailsId());
				if (optionalUserStats.isPresent()){
					BatchDetailsStatsEntity userStats = optionalUserStats.get();
					if ((userStats.getSentencesApproved() + userStats.getSentencesRejected()) < userStats.getSentencesTranslated()){
						int sentencesRejected = userStats.getSentencesRejected() + 1;
						userStats.setSentencesRejected(sentencesRejected);
						batchDetailsStatsRepository.save(userStats);

						TranslatedSentenceLogsEntity translatedSentenceLogs = getTranslatedSentenceLogsEntity(updatedSentence);
						translatedSentenceLogs.setDateModerated(new Date());
						translatedSentenceLogsRepo.save(translatedSentenceLogs);
					}
				}
			}
		}

		return ResponseEntity.ok(new ResponseMessage("Translation successfully rejected"));
	}

	public ResponseMessage correctTranslation(TranslateSentenceDto translatedSentenceText, Long translatedSentenceId) {

		TranslatedSentenceEntity translatedSentenceEntity = translatedRepo.findById(translatedSentenceId).get();
		if (Objects.nonNull(translatedSentenceText.getTranslatedText())) {
			translatedSentenceEntity.setTranslatedText(translatedSentenceText.getTranslatedText());
		}
		translatedRepo.save(translatedSentenceEntity);

		return new ResponseMessage("Translation correction has been submitted");
	}

	public TranslatedSentenceEntity expertApproveTranslatedSentence(Long id) {

		TranslatedSentenceEntity translatedStnc = translatedRepo.findById(id).get();

		translatedStnc.setSecondReview(StatusTypes.approved);
//		translatedStnc.setSeconds(timeTaken.getSeconds());

		TranslatedSentenceEntity updatedSentence = translatedRepo.save(translatedStnc);

		Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(translatedStnc.getBatchDetailsId());
		if (optionalBatchDetails.isPresent()){
			BatchDetailsEntity batchDetails = optionalBatchDetails.get();
			if (batchDetails.getBatchStatus() == BatchStatus.assignedExpertReviewer){ //Update user stats
				Optional<BatchDetailsStatsEntity> optionalUserStats = batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetails.getBatchDetailsId());
				if (optionalUserStats.isPresent()){
					BatchDetailsStatsEntity userStats = optionalUserStats.get();
					int noOfSentencesToReview = (int) Math.ceil(0.1 * batchDetails.getTranslatedSentence().size());
					if ((userStats.getSentencesExpertApproved() + userStats.getSentencesExpertRejected()) < noOfSentencesToReview){
						int sentencesExpertApproved = userStats.getSentencesExpertApproved() + 1;
						userStats.setSentencesExpertApproved(sentencesExpertApproved);
						batchDetailsStatsRepository.save(userStats);

						TranslatedSentenceLogsEntity translatedSentenceLogs = getTranslatedSentenceLogsEntity(updatedSentence);
						translatedSentenceLogs.setDateExpertModerated(new Date());
						translatedSentenceLogsRepo.save(translatedSentenceLogs);
					}
				}
			}
		}

		return updatedSentence;
	}

	public ResponseEntity<ResponseMessage> expertRejectTranslatedSentence(RejectTranslationDto rejectTranslationDto, String username) {

			ResponseEntity<ResponseMessage> validationResponse = validateDto(rejectTranslationDto);
			if (validationResponse != null)
				return validationResponse;

			Optional<TranslatedSentenceEntity> optionalTranslatedSentence = translatedRepo
					.findById(rejectTranslationDto.getTranslatedSentenceId());

			if (optionalTranslatedSentence.isEmpty())
				return ResponseEntity.badRequest().body(new ResponseMessage("Error! The translated sentence you are trying to reject does not exist"));

			TranslatedSentenceEntity translatedStnc = optionalTranslatedSentence.get();

			if (!translatedStnc.getBatchDetails().getSecondReviewer().getUsername().matches(username)){
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMessage("You are unauthorized to reject this translation"));
			}

			translatedStnc.setSecondReview(StatusTypes.rejected);
//		translatedStnc.setSeconds(timeTaken.getSeconds());

			ExpertCommentEntity expertCommentEntity =
					expertCommentRepo.findAllByTranslatedSentence_TranslatedSentenceId(translatedStnc.getTranslatedSentenceId());

			if (expertCommentEntity == null)
				expertCommentEntity = new ExpertCommentEntity();
			expertCommentEntity.setTranslatedSentence(translatedStnc);
			expertCommentEntity.setComment(rejectTranslationDto.getComment());

		TranslatedSentenceEntity updatedSentence = translatedRepo.save(translatedStnc);
		expertCommentRepo.save(expertCommentEntity);

		Optional<BatchDetailsEntity> optionalBatchDetails = batchDetailsRepo.findById(translatedStnc.getBatchDetailsId());
		if (optionalBatchDetails.isPresent()){
			BatchDetailsEntity batchDetails = optionalBatchDetails.get();
			if (batchDetails.getBatchStatus() == BatchStatus.assignedExpertReviewer){ //Update user stats
				Optional<BatchDetailsStatsEntity> optionalUserStats = batchDetailsStatsRepository.findByBatchDetailsBatchDetailsId(batchDetails.getBatchDetailsId());
				if (optionalUserStats.isPresent()){
					BatchDetailsStatsEntity userStats = optionalUserStats.get();
					int noOfSentencesToReview = (int) Math.ceil(0.1 * batchDetails.getTranslatedSentence().size());
					if ((userStats.getSentencesExpertApproved() + userStats.getSentencesExpertRejected()) < noOfSentencesToReview){
						int sentencesExpertRejected = userStats.getSentencesExpertRejected() + 1;
						userStats.setSentencesExpertRejected(sentencesExpertRejected);
						batchDetailsStatsRepository.save(userStats);

						TranslatedSentenceLogsEntity translatedSentenceLogs = getTranslatedSentenceLogsEntity(updatedSentence);
						translatedSentenceLogs.setDateExpertModerated(new Date());
						translatedSentenceLogsRepo.save(translatedSentenceLogs);
					}
				}
			}
		}

			return ResponseEntity.ok(new ResponseMessage("Translation successfully rejected "));
	}

	private static ResponseEntity<ResponseMessage> validateDto(RejectTranslationDto rejectTranslationDto) {
		if (rejectTranslationDto == null)
			return ResponseEntity.badRequest().body(new ResponseMessage("Please provide translated sentence id and comments"));
		if (rejectTranslationDto.getTranslatedSentenceId() == null)
			return ResponseEntity.badRequest().body(new ResponseMessage("Please provide translated sentence id"));
		if (rejectTranslationDto.getComment() == null || rejectTranslationDto.getComment().isBlank())
			return ResponseEntity.badRequest().body(new ResponseMessage("Please provide comments"));
		return null;
	}

	public List<TranslatedSentencesPerBatchDto> getTranslatedSentencesPerBatchDetails(Long batchDetailsId) {
		List<TranslatedSentenceEntity> translatedSentences =
				translatedRepo.findByBatchDetailsId(batchDetailsId, Sort.by(Sort.Direction.DESC, "reviewStatus"));
		List<TranslatedSentencesPerBatchDto> translatedSentencesPerBatchDtos = new ArrayList<>();

		for (TranslatedSentenceEntity translatedSentence: translatedSentences) {
			ModeratorCommentEntity moderatorCommentEntity = moderatorCommentRepo.findAllByTranslatedSentence_TranslatedSentenceId(translatedSentence.getTranslatedSentenceId());
			ExpertCommentEntity expertCommentEntity = expertCommentRepo.findAllByTranslatedSentence_TranslatedSentenceId(translatedSentence.getTranslatedSentenceId());

			String moderatorComment = "";
			String expertComment = "";

			if (translatedSentence.getReviewStatus() == StatusTypes.rejected && moderatorCommentEntity != null)
				moderatorComment = moderatorCommentEntity.getComment();
			if (translatedSentence.getSecondReview() == StatusTypes.rejected && expertCommentEntity != null)
				expertComment = expertCommentEntity.getComment();

			translatedSentence.getSentence().setAudioLink(amazonClient.generatePresignedUrl(translatedSentence.getSentence().getAudioLink()));
			TranslatedSentencesPerBatchDto sentencesPerBatchDto = new TranslatedSentencesPerBatchDto().toDto(
					translatedSentence,
					moderatorComment,
					expertComment
			);
			translatedSentencesPerBatchDtos.add(sentencesPerBatchDto);
		}
		return translatedSentencesPerBatchDtos;
    }

	@Transactional
	public ResponseEntity<ResponseMessage> deleteDuplicateTranslations() {
		translatedRepo.deleteDuplicateTranslations();
		return ResponseEntity.ok(new ResponseMessage("Duplicates successfully deleted"));
	}

	private TranslatedSentenceLogsEntity getTranslatedSentenceLogsEntity(TranslatedSentenceEntity updatedSentence) {
		TranslatedSentenceLogsEntity translatedSentenceLogs = translatedSentenceLogsRepo.findByTranslatedSentence(updatedSentence);
		if (translatedSentenceLogs == null){
			translatedSentenceLogs = new TranslatedSentenceLogsEntity();
			translatedSentenceLogs.setTranslatedSentence(updatedSentence);
		}
		return translatedSentenceLogs;
	}
}