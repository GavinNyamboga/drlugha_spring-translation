package kmusau.translator.repository;

import kmusau.translator.entity.CorrectedTranslatedSentences;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorrectedSentencesRepository extends JpaRepository<CorrectedTranslatedSentences, Long> {

}
