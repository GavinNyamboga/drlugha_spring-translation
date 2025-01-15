package drlugha.translator.repository;

import drlugha.translator.entity.AssignedSentencesEntity;
import drlugha.translator.enums.StatusTypes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranslateAssignmentRepository extends JpaRepository<AssignedSentencesEntity, Long> {
    List<AssignedSentencesEntity> findByTranslationStatusAndTranslatorUserId(StatusTypes translationStatus, Long userId);

}
