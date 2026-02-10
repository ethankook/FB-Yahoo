package com.example.fbyahoo.controller.ingestion;

import com.example.fbyahoo.service.ingestion.MatchupIngestionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/ingest/matchup")
public class MatchupIngestionController {

    private final MatchupIngestionService matchupIngestionService;

    public MatchupIngestionController(MatchupIngestionService matchupIngestionService) {
        this.matchupIngestionService = matchupIngestionService;
    }

    @GetMapping("/all")
    public String ingestAllMatchups() {
        matchupIngestionService.ingestAllMatchups();
        return "redirect:/success";
    }

    @GetMapping("/{leagueKey}")
    public String ingestMatchups(
            @PathVariable String leagueKey,
            @RequestParam(defaultValue = "1") int week
    ) {
        matchupIngestionService.ingestMatchups(leagueKey, week);
        return "redirect:/success";
    }
}
