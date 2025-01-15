package kmusau.translator.controller;

import kmusau.translator.DTOs.stats.StatsDTO;
import kmusau.translator.entity.UsersEntity;
import kmusau.translator.service.StatsService;
import kmusau.translator.service.UserDetailsImpl;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/stats")
    public List<StatsDTO> getStats(){
        return statsService.getStats();
    }
}
