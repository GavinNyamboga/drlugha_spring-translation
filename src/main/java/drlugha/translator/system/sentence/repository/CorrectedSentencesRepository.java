package drlugha.translator.system.sentence.repository;

import drlugha.translator.system.sentence.model.CorrectedTranslatedSentences;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorrectedSentencesRepository extends JpaRepository<CorrectedTranslatedSentences, Long> {

}
