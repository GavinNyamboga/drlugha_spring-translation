package drlugha.translator.system.language.controller;

import drlugha.translator.system.language.dto.LanguageDTO;
import drlugha.translator.system.language.model.Language;
import drlugha.translator.shared.enums.DeletionStatus;
import drlugha.translator.system.language.repository.LanguageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/languages")
public class LanguageController {

    private final LanguageRepository languageRepository;

    public LanguageController(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @GetMapping
    public List<Language> getLanguages() {
        return languageRepository.findAll();
    }

    @PostMapping("/add")
    public ResponseEntity<String> addLanguage(@RequestBody LanguageDTO languageDTO) {
        if (languageDTO == null || languageDTO.getLanguageName() == null || languageDTO.getLanguageName().isBlank()) {
            return ResponseEntity.badRequest().body("Please provide language name");
        }
        languageRepository.save(languageDTO.getEntity());
        return ResponseEntity.ok("Language created successfully");
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateLanguage(@RequestBody LanguageDTO languageDTO) {
        if (languageDTO == null) {
            return ResponseEntity.badRequest().body("Please provide language id and language name");
        }
        if (languageDTO.getLanguageId() == null) {
            return ResponseEntity.badRequest().body("Please provide language id");
        }
        if (languageDTO.getLanguageName() == null || languageDTO.getLanguageName().isBlank()) {
            return ResponseEntity.badRequest().body("Please provide language name");
        }
        Optional<Language> optionalLanguageEntity = languageRepository.findById(languageDTO.getLanguageId());
        if (optionalLanguageEntity.isEmpty())
            return ResponseEntity.badRequest().body("Language does not exist");
        Language entity = optionalLanguageEntity.get();
        entity.setName(languageDTO.getLanguageName());
        languageRepository.save(entity);
        return ResponseEntity.ok("Language updated successfully");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteLanguage(@RequestParam Long languageId) {
        if (languageId == null) {
            return ResponseEntity.badRequest().body("Please provide language id");
        }
        Optional<Language> optionalLanguageEntity = languageRepository.findById(languageId);
        if (optionalLanguageEntity.isEmpty())
            return ResponseEntity.badRequest().body("Language does not exist");
        Language entity = optionalLanguageEntity.get();
        entity.setDeletionStatus(DeletionStatus.DELETED);
        languageRepository.save(entity);
        return ResponseEntity.ok("Language successfully delete");
    }


}
