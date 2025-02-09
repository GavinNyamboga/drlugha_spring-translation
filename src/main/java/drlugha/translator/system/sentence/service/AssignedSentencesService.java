package drlugha.translator.system.sentence.service;

import drlugha.translator.system.batch.model.BatchDetailsEntity;
import drlugha.translator.system.batch.repository.BatchDetailsRepository;
import drlugha.translator.system.sentence.dto.AssignmentDTO;
import drlugha.translator.system.sentence.dto.AssignedSentenceDto;
import drlugha.translator.system.sentence.dto.SentencesToTranslateDto;
import drlugha.translator.system.sentence.model.AssignedSentencesEntity;
import drlugha.translator.system.sentence.model.Sentence;
import drlugha.translator.system.batch.enums.BatchStatus;
import drlugha.translator.system.batch.enums.BatchType;
import drlugha.translator.shared.enums.StatusTypes;
import drlugha.translator.system.sentence.repository.TranslateAssignmentRepository;
import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.system.sentence.repository.SentenceRepository;
import drlugha.translator.configs.AmazonClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignedSentencesService {

    private final BatchDetailsRepository batchDetailsRepo;

    private final TranslateAssignmentRepository assignmentRepo;

    private final SentenceRepository sentenceRepo;

    private final AmazonClient amazonClient;


    public ResponseEntity<ResponseMessage> createTasks(AssignmentDTO assignmentDto) {
        List<Sentence> dtoSentences = assignmentDto.getSentences();

        if (assignmentDto.getTranslatorId() == null || assignmentDto.getAssignedToReviewId() == null) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Translator Id or Reviewer Id cannot be empty"));
        }

        ArrayList<AssignedSentencesEntity> assignedSentences = new ArrayList<>();

        for (Sentence sentence1 : dtoSentences) {
            AssignmentDTO dto = new AssignmentDTO();
            dto.setSentenceId(sentence1.getSentenceId());

//            if (assignmentDto.getTranslateToLanguage() == Languages.Kikuyu) {
//                sentence1.setAssignedToKikuyu(true);
//                sentenceRepo.save(sentence1);
//            } else {
//                sentence1.setAssignedToKimeru(true);
//                sentenceRepo.save(sentence1);
//            }

            dto.setDateAssigned(new Date());
            dto.setTranslateToLanguage(assignmentDto.getTranslateToLanguage());
            dto.setTranslatorId(assignmentDto.getTranslatorId());
            dto.setAssignedToReviewId(assignmentDto.getAssignedToReviewId());
            dto.setTranslationStatus(StatusTypes.ASSIGNED);

            AssignedSentencesEntity assignment = new AssignmentDTO().DtoToEntity(dto);

            assignedSentences.add(assignment);
        }

        assignmentRepo.saveAll(assignedSentences);
        ResponseMessage responseMessage = new ResponseMessage(assignedSentences.size() + " sentences have been assigned");
        return ResponseEntity.ok().body(responseMessage);
    }

    public SentencesToTranslateDto fetchAssignedSentences(Long translatorId, BatchStatus batchStatus, Long batchDetailsId) {
        List<BatchDetailsEntity> batchDetails;
        if (batchDetailsId != null) {
            batchDetails = batchDetailsRepo.findByTranslatedByIdAndBatchDetailsId(translatorId, batchDetailsId);
        } else {
            batchDetails = batchDetailsRepo.findByTranslatedByIdAndBatchStatus(translatorId, batchStatus);
        }
        if (!batchDetails.isEmpty()) {
            List<Sentence> pendingSentences = new ArrayList<>();
            List<Sentence> translatedSentences = new ArrayList<>();
            List<AssignedSentenceDto> untranslatedSentencesDto = new ArrayList<>();
            List<AssignedSentenceDto> translatedSentencesDto = new ArrayList<>();
            String language = null;
            String batchType = null;

            for (BatchDetailsEntity batchDetail : batchDetails) {
                language = batchDetail.getLanguage().getName();
                batchType = batchDetail.getBatch().getBatchType().getName();
                batchDetailsId = batchDetail.getBatchDetailsId();
                Long batchNo = batchDetail.getBatch().getBatchNo();

                pendingSentences = sentenceRepo.findUnTranslatedSentences(batchNo, batchDetail.getLanguage());

                if (batchDetail.getBatch().getBatchType() == BatchType.AUDIO) {
                    untranslatedSentencesDto = pendingSentences.stream().map(sentenceEntity -> {
                                sentenceEntity.setAudioLink(amazonClient.generatePresignedUrl(sentenceEntity.getAudioLink()));
                                return new AssignedSentenceDto(sentenceEntity);
                            })
                            .collect(Collectors.toList());
                } else {
                    untranslatedSentencesDto = pendingSentences.stream().map(AssignedSentenceDto::new)
                            .collect(Collectors.toList());
                }

                if (!pendingSentences.isEmpty())
                    break;
            }

            return new SentencesToTranslateDto(batchDetailsId, language, batchType, null, untranslatedSentencesDto);
        }
        return new SentencesToTranslateDto();
    }

}
