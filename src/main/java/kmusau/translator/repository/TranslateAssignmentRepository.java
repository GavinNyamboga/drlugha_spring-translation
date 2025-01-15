package kmusau.translator.repository;

import kmusau.translator.entity.AssignedSentencesEntity;
import kmusau.translator.enums.StatusTypes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranslateAssignmentRepository extends JpaRepository<AssignedSentencesEntity, Long> {
    List<AssignedSentencesEntity> findByTranslationStatusAndTranslatorUserId(StatusTypes translationStatus, Long userId);

}
