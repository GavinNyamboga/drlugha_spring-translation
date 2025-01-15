package kmusau.translator.service;

import kmusau.translator.DTOs.assignmentDTOs.AssignmentDto;
import kmusau.translator.DTOs.sentenceDTOs.AssignedSentenceDto;
import kmusau.translator.DTOs.sentenceDTOs.SentencesToTranslateDto;
import kmusau.translator.entity.AssignedSentencesEntity;
import kmusau.translator.entity.BatchDetailsEntity;
import kmusau.translator.entity.SentenceEntity;
import kmusau.translator.enums.BatchStatus;
import kmusau.translator.enums.BatchType;
import kmusau.translator.enums.StatusTypes;
import kmusau.translator.repository.*;
import kmusau.translator.response.ResponseMessage;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssignedSentencesService {

    @Autowired
    UserRepository userRepo;

    @Autowired
    BatchDetailsRepository batchDetailsRepo;

    @Autowired
    TranslateAssignmentRepository assignmentRepo;

    @Autowired
    SentenceRepository sentenceRepo;

    @Autowired
    TranslatedSentenceRepository translatedSentenceRepo;

    @Autowired
    Logger logger;

    @Autowired
    AmazonClient amazonClient;


    public ResponseEntity<ResponseMessage> createTasks(AssignmentDto assignmentDto) {
        List<SentenceEntity> dtoSentences = assignmentDto.getSentences();

        if (assignmentDto.getTranslatorId() == null || assignmentDto.getAssignedToReviewId() == null) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Translator Id or Reviewer Id cannot be empty"));
        }

        ArrayList<AssignedSentencesEntity> assignedSentences = new ArrayList<>();

        for (SentenceEntity sentence1: dtoSentences) {
            AssignmentDto dto = new AssignmentDto();
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
            dto.setTranslationStatus(StatusTypes.assigned);

            AssignedSentencesEntity assignment = new AssignmentDto().DtoToEntity(dto);

            assignedSentences.add(assignment);
        }

        assignmentRepo.saveAll(assignedSentences);
        ResponseMessage responseMessage = new ResponseMessage(assignedSentences.size()+ " sentences have been assigned");
        return ResponseEntity.ok().body(responseMessage);
    }

    public SentencesToTranslateDto fetchAssignedSentences(Long translatorId, BatchStatus batchStatus, Long batchDetailsId) {
        List<BatchDetailsEntity> batchDetails;
        if (batchDetailsId != null) {
            batchDetails = batchDetailsRepo.findByTranslatedByIdAndBatchDetailsId(translatorId, batchDetailsId);
        } else {
            batchDetails = batchDetailsRepo.findByTranslatedByIdAndBatchStatus(translatorId, batchStatus);
        }
        if (!batchDetails.isEmpty())  {
            List<SentenceEntity> pendingSentences = new ArrayList<>();
            List<SentenceEntity> translatedSentences = new ArrayList<>();
            List<AssignedSentenceDto> untranslatedSentencesDto = new ArrayList<>();
            List<AssignedSentenceDto> translatedSentencesDto = new ArrayList<>();
            String language = null;
            String batchType = null;

            for (BatchDetailsEntity batchDetail: batchDetails){
                language = batchDetail.getLanguage().getName();
                batchType = batchDetail.getBatch().getBatchType().getName();
                batchDetailsId = batchDetail.getBatchDetailsId();
                Long batchNo = batchDetail.getBatch().getBatchNo();

                pendingSentences = sentenceRepo.findUnTranslatedSentences(batchNo, batchDetail.getLanguage());

                if (batchDetail.getBatch().getBatchType() == BatchType.AUDIO){
                    untranslatedSentencesDto = pendingSentences.stream().map(sentenceEntity -> {
                                sentenceEntity.setAudioLink(amazonClient.generatePresignedUrl(sentenceEntity.getAudioLink()));
                                return new AssignedSentenceDto(sentenceEntity);
                            })
                            .collect(Collectors.toList());
                }
                else {
                    untranslatedSentencesDto = pendingSentences.stream().map(AssignedSentenceDto::new)
                            .collect(Collectors.toList());
                }

                if (!pendingSentences.isEmpty())
                    break;
            }

            return new SentencesToTranslateDto(batchDetailsId, language, batchType,  null, untranslatedSentencesDto);
        }
        return new SentencesToTranslateDto();
    }

}
