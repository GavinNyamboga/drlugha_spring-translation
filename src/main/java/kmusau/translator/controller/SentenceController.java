package kmusau.translator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import kmusau.translator.DTOs.sentenceDTOs.CreateSentenceDto;
import kmusau.translator.entity.SentenceEntity;
import kmusau.translator.repository.SentenceRepository;
import kmusau.translator.response.ResponseMessage;
import kmusau.translator.service.SentenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SentenceController {
    @Autowired
    SentenceRepository sentenceRepo;

    @Autowired
    SentenceService sentenceSvc;

    @Autowired
    ObjectMapper objectMapper;

    @GetMapping({"/fetch/sentence"})
    public List<SentenceEntity> allSentences(@RequestParam(defaultValue = "0") int pageNo, int size) {
        return this.sentenceSvc.getSentencesByPage(pageNo, size);
    }

    @GetMapping({"/all/sentence"})
    public List<SentenceEntity> totalSentences() {
        return this.sentenceRepo.findAll();
    }

    @PostMapping({"/create/sentence"})
    public ResponseEntity<ResponseMessage> addSentence(@RequestHeader("Authorization") String authorizationHeader, @RequestBody CreateSentenceDto sentenceDto) throws Exception {
        return this.sentenceSvc.createSentence(authorizationHeader, sentenceDto);
    }

    @PostMapping({"upload/sentences/{batchNo}"})
    public ResponseEntity<ResponseMessage> addSentences(@RequestBody List<CreateSentenceDto> sentenceDto, @PathVariable Long batchNo) {
        return this.sentenceSvc.addSentences(sentenceDto, batchNo);
    }

    @GetMapping({"/fetch/sentence/{id}"})
    public Optional<SentenceEntity> singleSentence(@PathVariable Long id) {
        return this.sentenceRepo.findById(id);
    }

    @PutMapping({"/update/sentence/{id}"})
    public ResponseMessage updateSentence(@RequestBody SentenceEntity sentence, @PathVariable Long id) throws JsonProcessingException {
        try {
            SentenceEntity updatedSentence = this.sentenceSvc.editSentence(sentence, id);
            return new ResponseMessage("Updated sentence" + this.objectMapper.writeValueAsString(updatedSentence));
        } catch (NoSuchElementException e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @DeleteMapping({"/delete/sentence/{id}"})
    public ResponseMessage deleteSentence(@PathVariable Long id) {
        try {
            this.sentenceRepo.deleteById(id);
            return new ResponseMessage("Deleted successfully");
        } catch (EmptyResultDataAccessException e) {
            return new ResponseMessage(e.getMessage());
        }
    }
}
