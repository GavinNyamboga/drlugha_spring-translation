package drlugha.translator.repository;

import drlugha.translator.entity.LanguageEntity;
import drlugha.translator.entity.SentenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SentenceRepository extends JpaRepository<SentenceEntity, Long> {

    Integer countAllByBatchNo(Long batchNo);

    @Query("SELECT s FROM SentenceEntity s WHERE s.batchNo = :batchNumber AND " +
            "s.sentenceId NOT IN (SELECT t.sentenceId FROM TranslatedSentenceEntity t WHERE t.sentence.batchNo = :batchNumber AND t.language = :language )")
    List<SentenceEntity> findUnTranslatedSentences(Long batchNumber, LanguageEntity language);

    void deleteAllByBatchNo(Long batchNo);

}
