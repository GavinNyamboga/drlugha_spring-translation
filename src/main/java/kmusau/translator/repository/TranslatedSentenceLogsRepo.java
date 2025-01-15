package kmusau.translator.repository;

import kmusau.translator.entity.TranslatedSentenceEntity;
import kmusau.translator.entity.TranslatedSentenceLogsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranslatedSentenceLogsRepo extends JpaRepository<TranslatedSentenceLogsEntity, Long> {
    TranslatedSentenceLogsEntity findByTranslatedSentence(TranslatedSentenceEntity translatedSentence);
}
