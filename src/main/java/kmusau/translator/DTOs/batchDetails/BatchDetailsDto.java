package kmusau.translator.DTOs.batchDetails;

import kmusau.translator.DTOs.userDTOs.UserDetailDto;
import kmusau.translator.entity.BatchDetailsEntity;
import kmusau.translator.enums.BatchStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchDetailsDto {

    private Long batchDetailsId;

    private Long language;

    private BatchStatus batchStatus;

    private Long batchId;

    private UserDetailDto translatedBy;

    private UserDetailDto translationVerifiedBy;

    private UserDetailDto secondReviewer;

    private UserDetailDto recordedBy;

    private UserDetailDto audioVerifiedBy;

    public BatchDetailsDto entityToDto(BatchDetailsEntity batchDetailsEntity) {
        BatchDetailsDto batchDetailsDto = new BatchDetailsDto();

        if (batchDetailsEntity == null) {
            return new BatchDetailsDto();
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
            batchDetailsDto.setTranslatedBy(new UserDetailDto().toDto(batchDetailsEntity.getTranslatedBy()));
        if (batchDetailsEntity.getTranslationVerifiedBy() != null)
            batchDetailsDto.setTranslationVerifiedBy(new UserDetailDto().toDto(batchDetailsEntity.getTranslationVerifiedBy()));
        if (batchDetailsEntity.getSecondReviewer() != null)
            batchDetailsDto.setSecondReviewer(new UserDetailDto().toDto(batchDetailsEntity.getSecondReviewer()));
        if (batchDetailsEntity.getRecordedBy() != null)
            batchDetailsDto.setRecordedBy(new UserDetailDto().toDto(batchDetailsEntity.getRecordedBy()));
        if (batchDetailsEntity.getAudioVerifiedBy() != null)
            batchDetailsDto.setAudioVerifiedBy(new UserDetailDto().toDto(batchDetailsEntity.getAudioVerifiedBy()));

        return batchDetailsDto;
    }

}
