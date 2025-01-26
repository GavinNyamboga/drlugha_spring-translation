package drlugha.translator.system.stats.service;

import drlugha.translator.system.stats.dto.StatsDTO;
import drlugha.translator.system.sentence.repository.TranslatedSentenceRepository;
import drlugha.translator.system.user.repository.UserRepository;
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
