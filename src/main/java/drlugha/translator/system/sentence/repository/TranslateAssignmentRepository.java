package drlugha.translator.system.sentence.repository;

import drlugha.translator.system.sentence.model.AssignedSentencesEntity;
import drlugha.translator.shared.enums.StatusTypes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranslateAssignmentRepository extends JpaRepository<AssignedSentencesEntity, Long> {
    List<AssignedSentencesEntity> findByTranslationStatusAndTranslatorUserId(StatusTypes translationStatus, Long userId);

}
