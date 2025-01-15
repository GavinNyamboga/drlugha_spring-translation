package drlugha.translator.service;

import drlugha.translator.DTOs.stats.StatsDTO;
import drlugha.translator.repository.TranslatedSentenceRepository;
import drlugha.translator.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class StatsService {
    private TranslatedSentenceRepository translatedSentenceRepository;

    private UserRepository userRepository;

    public List<StatsDTO> getStats() {
        return userRepository.generateStats();
    }
}
