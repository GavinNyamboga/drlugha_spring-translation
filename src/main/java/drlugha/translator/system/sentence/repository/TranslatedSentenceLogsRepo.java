package drlugha.translator.system.sentence.repository;

import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.system.sentence.model.TranslatedSentenceLogsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranslatedSentenceLogsRepo extends JpaRepository<TranslatedSentenceLogsEntity, Long> {
    TranslatedSentenceLogsEntity findByTranslatedSentence(TranslatedSentenceEntity translatedSentence);
}
