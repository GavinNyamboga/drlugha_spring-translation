package drlugha.translator.controller;

import drlugha.translator.DTOs.stats.StatsDTO;
import drlugha.translator.service.StatsService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/stats")
    public List<StatsDTO> getStats() {
        return statsService.getStats();
    }
}
