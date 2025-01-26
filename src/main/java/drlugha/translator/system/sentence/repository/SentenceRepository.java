package drlugha.translator.system.sentence.repository;

import drlugha.translator.system.language.model.Language;
import drlugha.translator.system.sentence.model.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SentenceRepository extends JpaRepository<Sentence, Long> {

    Integer countAllByBatchNo(Long batchNo);

    @Query("SELECT s FROM Sentence s WHERE s.batchNo = :batchNumber AND " +
            "s.sentenceId NOT IN (SELECT t.sentenceId FROM TranslatedSentenceEntity t WHERE t.sentence.batchNo = :batchNumber AND t.language = :language )")
    List<Sentence> findUnTranslatedSentences(Long batchNumber, Language language);

    void deleteAllByBatchNo(Long batchNo);

}
