package drlugha.translator.system.sentence.service;

import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.shared.exception.GeneralException;
import drlugha.translator.system.sentence.dto.CreateSentenceDTO;
import drlugha.translator.system.sentence.model.Sentence;
import drlugha.translator.system.sentence.repository.SentenceRepository;
import drlugha.translator.util.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    public List<Sentence> getSentencesByPage(int pageNo, int size) {
        PageRequest pageRequest = PageRequest.of(pageNo, size);
        Page<Sentence> pagedResult = this.sentenceRepo.findAll(pageRequest);
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

        username = this.jwtUtil.extractUsername(authorizationHeader);

        Sentence sentence = modelMapper.map(sentenceDto, Sentence.class);
        sentence.setDateCreated(new Date());
        this.sentenceRepo.save(sentence);
        ResponseMessage responseMessage = new ResponseMessage("Sentence saved");
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    public Sentence editSentence(Sentence sentence, Long id) {
        Sentence stnc = this.sentenceRepo.findById(id).get();
        if (Objects.nonNull(sentence.getSentenceText()))
            stnc.setSentenceText(sentence.getSentenceText());
        if (Objects.nonNull(sentence.getDateCreated()))
            stnc.setDateCreated(sentence.getDateCreated());
        return this.sentenceRepo.save(stnc);
    }

    public ResponseMessage addSentences(List<CreateSentenceDTO> sentenceDto, Long batchNo) {
        List<Sentence> sentences = mapList(sentenceDto, Sentence.class);
        try {
            for (Sentence sentence : sentences) {
                sentence.setDateCreated(new Date());
                sentence.setBatchNo(batchNo);
            }
            this.sentenceRepo.saveAll(sentences);
            return new ResponseMessage("Uploaded " + sentences.size() + " sentences successfully");
        } catch (Exception e) {
            throw new GeneralException(e.getMessage());
        }
    }
}
