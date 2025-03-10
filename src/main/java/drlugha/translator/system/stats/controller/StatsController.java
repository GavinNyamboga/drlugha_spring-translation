package drlugha.translator.system.stats.controller;

import drlugha.translator.shared.controller.BaseController;
import drlugha.translator.system.stats.dto.LanguageStatisticsDTO;
import drlugha.translator.system.stats.dto.StatsDTO;
import drlugha.translator.system.stats.service.StatsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/stats")
public class StatsController extends BaseController {

    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<List<StatsDTO>> getStats() {
        return entity(statsService.getStats());
    }

    @GetMapping("/languages")
    public ResponseEntity<List<LanguageStatisticsDTO>> getLanguageStats() {
        return entity(statsService.getLanguageStats());
    }
}
