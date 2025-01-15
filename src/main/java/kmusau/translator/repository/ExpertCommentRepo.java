package kmusau.translator.repository;

import kmusau.translator.entity.ExpertCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertCommentRepo extends JpaRepository<ExpertCommentEntity, Long> {
    ExpertCommentEntity findAllByTranslatedSentence_TranslatedSentenceId(Long translatedSentenceId);
}

