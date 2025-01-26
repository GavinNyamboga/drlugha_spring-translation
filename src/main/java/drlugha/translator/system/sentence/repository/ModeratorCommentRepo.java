package drlugha.translator.system.sentence.repository;

import drlugha.translator.system.sentence.model.ModeratorCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModeratorCommentRepo extends JpaRepository<ModeratorCommentEntity, Long> {
    ModeratorCommentEntity findAllByTranslatedSentence_TranslatedSentenceId(Long translatedSentenceId);
}
