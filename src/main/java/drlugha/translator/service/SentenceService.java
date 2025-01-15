package drlugha.translator.service;

import drlugha.translator.DTOs.sentenceDTOs.CreateSentenceDTO;
import drlugha.translator.entity.SentenceEntity;
import drlugha.translator.repository.SentenceRepository;
import drlugha.translator.response.ResponseMessage;
import drlugha.translator.util.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SentenceService {
    private final SentenceRepository sentenceRepo;

    private final JwtUtil jwtUtil;

    public SentenceService(SentenceRepository sentenceRepo, JwtUtil jwtUtil) {
        this.sentenceRepo = sentenceRepo;
        this.jwtUtil = jwtUtil;
    }

    private static final ModelMapper modelMapper = new ModelMapper();

    <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source
                .stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }

    public List<SentenceEntity> getSentencesByPage(int pageNo, int size) {
        PageRequest pageRequest = PageRequest.of(pageNo, size);
        Page<SentenceEntity> pagedResult = this.sentenceRepo.findAll(pageRequest);
        if (pagedResult.hasContent())
            return pagedResult.getContent();
        return new ArrayList<>();
    }

    public ResponseEntity<ResponseMessage> createSentence(@RequestHeader("Authorization") String authorizationHeader, CreateSentenceDTO sentenceDto) throws Exception {
        String username = null;
        String jwt = null;
        if (sentenceDto.getSentenceText() == null) {
            ResponseMessage responseMessage1 = new ResponseMessage("Sentence text cannot be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage1);
        }
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = this.jwtUtil.extractUsername(jwt);
        }
        SentenceEntity sentence = modelMapper.map(sentenceDto, SentenceEntity.class);
        sentence.setDateCreated(new Date());
        this.sentenceRepo.save(sentence);
        ResponseMessage responseMessage = new ResponseMessage("Sentence saved");
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    public SentenceEntity editSentence(SentenceEntity sentence, Long id) {
        SentenceEntity stnc = this.sentenceRepo.findById(id).get();
        if (Objects.nonNull(sentence.getSentenceText()))
            stnc.setSentenceText(sentence.getSentenceText());
        if (Objects.nonNull(sentence.getDateCreated()))
            stnc.setDateCreated(sentence.getDateCreated());
        SentenceEntity updatedSentence = this.sentenceRepo.save(stnc);
        return updatedSentence;
    }

    public ResponseEntity<ResponseMessage> addSentences(List<CreateSentenceDTO> sentenceDto, Long batchNo) {
        List<SentenceEntity> sentences = mapList(sentenceDto, SentenceEntity.class);
        try {
            for (SentenceEntity sentence : sentences) {
                sentence.setDateCreated(new Date());
                sentence.setBatchNo(batchNo);
            }
            this.sentenceRepo.saveAll(sentences);
            ResponseMessage responseMessage = new ResponseMessage("Uploaded " + sentences.size() + " sentences successfully");
            return ResponseEntity.ok().body(responseMessage);
        } catch (Exception e) {
            ResponseMessage responseMessage = new ResponseMessage(e.getMessage());
            return ResponseEntity.internalServerError().body(responseMessage);
        }
    }
}
