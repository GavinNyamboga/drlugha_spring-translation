package drlugha.translator.system.batch.dto;

import drlugha.translator.system.batch.enums.BatchStatus;
import drlugha.translator.system.batch.model.BatchDetailsEntity;
import drlugha.translator.system.user.dto.UserDetailDTO;
import drlugha.translator.system.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchDetailsDTO {

    private Long batchDetailsId;

    private Long language;

    private BatchStatus batchStatus;

    private Long batchId;

    private UserDetailDTO translatedBy;

    private UserDetailDTO translationVerifiedBy;

    private UserDetailDTO secondReviewer;

    private List<UserDetailDTO> recordedBy = new ArrayList<>();

    private UserDetailDTO audioVerifiedBy;
    private UserDetailDTO audioExpertReviewedBy;

    public BatchDetailsDTO entityToDto(BatchDetailsEntity batchDetailsEntity) {
        BatchDetailsDTO batchDetailsDto = new BatchDetailsDTO();

        if (batchDetailsEntity == null) {
            return new BatchDetailsDTO();
        }

        if (batchDetailsEntity.getBatchDetailsId() != null)
            batchDetailsDto.setBatchDetailsId(batchDetailsEntity.getBatchDetailsId());
        if (batchDetailsEntity.getLanguage() != null)
            batchDetailsDto.setLanguage(batchDetailsEntity.getLanguage().getLanguageId());
        if (batchDetailsEntity.getBatchStatus() != null)
            batchDetailsDto.setBatchStatus(batchDetailsEntity.getBatchStatus());
        if (batchDetailsEntity.getBatchId() != null)
            batchDetailsDto.setBatchId(batchDetailsEntity.getBatchId());
        if (batchDetailsEntity.getTranslatedBy() != null)
            batchDetailsDto.setTranslatedBy(new UserDetailDTO().toDto(batchDetailsEntity.getTranslatedBy()));
        if (batchDetailsEntity.getTranslationVerifiedBy() != null)
            batchDetailsDto.setTranslationVerifiedBy(new UserDetailDTO().toDto(batchDetailsEntity.getTranslationVerifiedBy()));
        if (batchDetailsEntity.getSecondReviewer() != null)
            batchDetailsDto.setSecondReviewer(new UserDetailDTO().toDto(batchDetailsEntity.getSecondReviewer()));
        if (batchDetailsEntity.getAudioVerifiedBy() != null)
            batchDetailsDto.setAudioVerifiedBy(new UserDetailDTO().toDto(batchDetailsEntity.getAudioVerifiedBy()));

        batchDetailsEntity.getBatchDetailsUserAssignment().forEach(batchDetailsUserAssignment -> {
            User user = batchDetailsUserAssignment.getUser();
            if (user != null) {
                switch (batchDetailsUserAssignment.getBatchRole()) {
                    case TEXT_TRANSLATOR:
                        batchDetailsDto.setTranslatedBy(new UserDetailDTO().toDto(user));
                        break;
                    case TEXT_VERIFIER:
                        batchDetailsDto.setTranslationVerifiedBy(new UserDetailDTO().toDto(user));
                        break;
                    case EXPERT_TEXT_REVIEWER:
                        batchDetailsDto.setSecondReviewer(new UserDetailDTO().toDto(user));
                        break;
                    case AUDIO_RECORDER:
                        batchDetailsDto.getRecordedBy().add(new UserDetailDTO().toDto(user));
                        break;
                    case AUDIO_VERIFIER:
                        batchDetailsDto.setAudioVerifiedBy(new UserDetailDTO().toDto(user));
                        break;
                    case EXPERT_AUDIO_REVIEWER:
                        batchDetailsDto.setAudioExpertReviewedBy(new UserDetailDTO().toDto(user));
                        break;
                    default:
                        break;
                }
            }

        });

        return batchDetailsDto;
    }

}
