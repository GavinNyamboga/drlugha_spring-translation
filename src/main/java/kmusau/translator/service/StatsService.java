package kmusau.translator.service;

import kmusau.translator.DTOs.stats.StatsDTO;
import kmusau.translator.entity.UsersEntity;
import kmusau.translator.enums.BatchStatus;
import kmusau.translator.repository.TranslatedSentenceRepository;
import kmusau.translator.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StatsService {
    private TranslatedSentenceRepository translatedSentenceRepository;

    private UserRepository userRepository;

    public List<StatsDTO> getStats() {
        return userRepository.generateStats();
    }
}
