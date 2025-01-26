package drlugha.translator.system.language.repository;

import drlugha.translator.system.language.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<Language, Long> {

    Language findByName(String name);
}
