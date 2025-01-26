package drlugha.translator.system.sentence.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drlugha.translator.system.sentence.dto.CreateSentenceDTO;
import drlugha.translator.system.sentence.model.Sentence;
import drlugha.translator.system.sentence.repository.SentenceRepository;
import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.system.sentence.service.SentenceService;
import drlugha.translator.shared.controller.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class SentenceController extends BaseController {

    private final SentenceRepository sentenceRepo;

    private final SentenceService sentenceService;

    private final ObjectMapper objectMapper;

    @GetMapping({"/fetch/sentence"})
    public List<Sentence> allSentences(@RequestParam(defaultValue = "0") int pageNo, int size) {
        return sentenceService.getSentencesByPage(pageNo, size);
    }

    @GetMapping({"/all/sentence"})
    public List<Sentence> totalSentences() {
        return sentenceRepo.findAll();
    }

    @PostMapping({"/create/sentence"})
    public ResponseEntity<ResponseMessage> addSentence(@RequestHeader("Authorization") String authorizationHeader, @RequestBody CreateSentenceDTO sentenceDto) throws Exception {
        return sentenceService.createSentence(authorizationHeader, sentenceDto);
    }

    @PostMapping({"upload/sentences/{batchNo}"})
    public ResponseEntity<ResponseMessage> addSentences(@RequestBody List<CreateSentenceDTO> sentenceDto, @PathVariable Long batchNo) {
        return entity(sentenceService.addSentences(sentenceDto, batchNo));
    }

    @GetMapping({"/fetch/sentence/{id}"})
    public Optional<Sentence> singleSentence(@PathVariable Long id) {
        return sentenceRepo.findById(id);
    }

    @PutMapping({"/update/sentence/{id}"})
    public ResponseMessage updateSentence(@RequestBody Sentence sentence, @PathVariable Long id) throws JsonProcessingException {
        try {
            Sentence updatedSentence = sentenceService.editSentence(sentence, id);
            return new ResponseMessage("Updated sentence" + objectMapper.writeValueAsString(updatedSentence));
        } catch (NoSuchElementException e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @DeleteMapping({"/delete/sentence/{id}"})
    public ResponseMessage deleteSentence(@PathVariable Long id) {
        try {
            sentenceRepo.deleteById(id);
            return new ResponseMessage("Deleted successfully");
        } catch (EmptyResultDataAccessException e) {
            return new ResponseMessage(e.getMessage());
        }
    }
}
