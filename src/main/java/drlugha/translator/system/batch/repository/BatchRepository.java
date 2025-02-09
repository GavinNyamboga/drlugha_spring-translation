package drlugha.translator.system.batch.repository;

import drlugha.translator.shared.enums.YesNo;
import drlugha.translator.system.batch.enums.BatchOrigin;
import drlugha.translator.system.batch.enums.BatchType;
import drlugha.translator.system.batch.model.BatchEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BatchRepository extends JpaRepository<BatchEntity, Long> {

    @Query("SELECT b FROM BatchEntity b WHERE b.batchType=:batchType and b.deletionStatus=0  AND (:origin IS NULL OR b.batchOrigin=:origin)")
    Page<BatchEntity> findAllByBatchType(BatchType batchType, Pageable pageable, @Param("origin") BatchOrigin origin);

    @Query("SELECT b FROM BatchEntity b WHERE b.batchType=:batchType AND b.fromFeedback=:fromFeedback AND b.deletionStatus=0 " +
            "AND (:origin IS NULL OR b.batchOrigin=:origin)")
    Page<BatchEntity> findAllByBatchTypeAndFromFeedback(@NonNull BatchType batchType, YesNo fromFeedback, Pageable pageable,
                                                        @Param("origin") BatchOrigin origin);

    @Query("SELECT b FROM BatchEntity b where b.fromFeedback=:fromFeedback and b.targetLanguage.languageId=:targetLanguageId and " +
            "b.sourceLanguage.languageId=:sourceLanguageId")
    BatchEntity findByTargetLanguage_LanguageIdAndSourceLanguage_LanguageIdAndFromFeedback(Long targetLanguageId, Long sourceLanguageId, YesNo fromFeedback);

}
