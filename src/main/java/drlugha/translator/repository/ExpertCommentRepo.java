package drlugha.translator.repository;

import drlugha.translator.entity.ExpertCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertCommentRepo extends JpaRepository<ExpertCommentEntity, Long> {
    ExpertCommentEntity findAllByTranslatedSentence_TranslatedSentenceId(Long translatedSentenceId);
}

