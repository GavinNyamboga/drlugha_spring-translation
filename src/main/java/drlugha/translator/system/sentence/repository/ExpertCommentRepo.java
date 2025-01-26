package drlugha.translator.system.sentence.repository;

import drlugha.translator.system.sentence.model.ExpertCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertCommentRepo extends JpaRepository<ExpertCommentEntity, Long> {
    ExpertCommentEntity findAllByTranslatedSentence_TranslatedSentenceId(Long translatedSentenceId);
}

