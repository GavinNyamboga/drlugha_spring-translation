package drlugha.translator.repository;

import drlugha.translator.entity.BatchEntity;
import drlugha.translator.enums.BatchType;
import drlugha.translator.enums.YesNo;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BatchRepository extends JpaRepository<BatchEntity, Long> {
    List<BatchEntity> findAllByBatchType(BatchType batchType);

    @Query("SELECT b FROM BatchEntity b WHERE b.batchType=:batchType and b.fromFeedback=:fromFeedback")
    List<BatchEntity> findAllByBatchTypeAndFromFeedback(@NonNull BatchType batchType, YesNo fromFeedback);

    @Query("SELECT b FROM BatchEntity b where b.fromFeedback=:fromFeedback and b.targetLanguage.languageId=:targetLanguageId and " +
            "b.sourceLanguage.languageId=:sourceLanguageId")
    BatchEntity findByTargetLanguage_LanguageIdAndSourceLanguage_LanguageIdAndFromFeedback(Long targetLanguageId, Long sourceLanguageId, YesNo fromFeedback);

}
