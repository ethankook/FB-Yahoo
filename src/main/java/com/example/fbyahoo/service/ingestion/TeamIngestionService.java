package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.League;
import com.example.fbyahoo.repo.LeagueRepository;
import com.example.fbyahoo.repo.TeamRepository;
import com.example.fbyahoo.service.TokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TeamIngestionService {

    private static final Logger log = LoggerFactory.getLogger(LeagueIngestionService.class);


    private final WebClient fantasyClient;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;

    public TeamIngestionService(
            @Qualifier("yahooFantasyClient") WebClient fantasyClient,
            TokenService tokenService,
            ObjectMapper objectMapper,
            TeamRepository teamRepository,
            LeagueRepository leagueRepository
    )
    {
        this.fantasyClient = fantasyClient;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
        this.teamRepository = teamRepository;
        this.leagueRepository = leagueRepository;
    }

    @Transactional
    public void usurpLeague(String leagueKey) {

    }






}
