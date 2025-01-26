package drlugha.translator.system.stats.controller;

import drlugha.translator.system.stats.dto.StatsDTO;
import drlugha.translator.system.stats.service.StatsService;
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
