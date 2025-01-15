package drlugha.translator.repository;

import drlugha.translator.entity.TranslatedSentenceEntity;
import drlugha.translator.entity.TranslatedSentenceLogsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranslatedSentenceLogsRepo extends JpaRepository<TranslatedSentenceLogsEntity, Long> {
    TranslatedSentenceLogsEntity findByTranslatedSentence(TranslatedSentenceEntity translatedSentence);
}
