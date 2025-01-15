package drlugha.translator.repository;

import drlugha.translator.entity.BatchEntity;
import drlugha.translator.enums.BatchType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchRepository extends JpaRepository<BatchEntity, Long> {
    public List<BatchEntity> findAllByBatchType(BatchType batchType);

    BatchEntity findBySource(String source);

    BatchEntity findBySourceAndSourceLanguage_LanguageId(String userFeedback, Long languageId);

}
