package drlugha.translator.repository;

import drlugha.translator.entity.CorrectedTranslatedSentences;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorrectedSentencesRepository extends JpaRepository<CorrectedTranslatedSentences, Long> {

}
