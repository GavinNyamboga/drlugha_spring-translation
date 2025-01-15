package drlugha.translator.repository;

import drlugha.translator.entity.LanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<LanguageEntity, Long> {

    LanguageEntity findByName(String name);
}
