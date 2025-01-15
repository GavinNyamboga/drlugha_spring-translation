package kmusau.translator.repository;

import kmusau.translator.entity.ModeratorCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModeratorCommentRepo extends JpaRepository<ModeratorCommentEntity, Long> {
    ModeratorCommentEntity findAllByTranslatedSentence_TranslatedSentenceId(Long translatedSentenceId);
}
