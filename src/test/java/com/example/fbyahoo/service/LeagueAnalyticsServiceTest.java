package com.example.fbyahoo.service;

import com.example.fbyahoo.dto.api.CategoryRankDto;
import com.example.fbyahoo.dto.api.InsightsDto;
import com.example.fbyahoo.model.LeagueRosteredPlayer;
import com.example.fbyahoo.model.Player;
import com.example.fbyahoo.model.PlayerStats;
import com.example.fbyahoo.model.Team;
import com.example.fbyahoo.repo.LeagueRosteredPlayerRepository;
import com.example.fbyahoo.repo.PlayerStatsRepository;
import com.example.fbyahoo.repo.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeagueAnalyticsServiceTest {

    @Mock
    TeamRepository teamRepository;

    @Mock
    LeagueRosteredPlayerRepository lrpRepository;

    @Mock
    PlayerStatsRepository playerStatsRepository;

    private LeagueAnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new LeagueAnalyticsService(teamRepository, lrpRepository, playerStatsRepository);
    }

    @Test
    void computeInsights_returnsEmptyWhenNoOwnedTeam() {
        Team t1 = new Team();
        t1.setTeamKey("t1");
        t1.setIsOwnedByCurrentLogin(false);

        when(teamRepository.findByLeague_LeagueKey("l1")).thenReturn(List.of(t1));

        InsightsDto dto = service.computeInsights("l1");

        assertTrue(dto.myAverages().isEmpty());
        assertTrue(dto.strongest().isEmpty());
        assertTrue(dto.weakest().isEmpty());
    }

    @Test
    void computeInsights_ranksTurnoversAsLowerIsBetter() {
        Team myTeam = new Team();
        myTeam.setTeamKey("t1");
        myTeam.setIsOwnedByCurrentLogin(true);

        Team otherTeam = new Team();
        otherTeam.setTeamKey("t2");
        otherTeam.setIsOwnedByCurrentLogin(false);

        when(teamRepository.findByLeague_LeagueKey("l1")).thenReturn(List.of(myTeam, otherTeam));

        Player p1 = new Player();
        p1.setPlayerId("p1");
        Player p2 = new Player();
        p2.setPlayerId("p2");

        LeagueRosteredPlayer lrp1 = new LeagueRosteredPlayer();
        lrp1.setPlayer(p1);
        LeagueRosteredPlayer lrp2 = new LeagueRosteredPlayer();
        lrp2.setPlayer(p2);

        when(lrpRepository.findByTeam_TeamKey("t1")).thenReturn(List.of(lrp1));
        when(lrpRepository.findByTeam_TeamKey("t2")).thenReturn(List.of(lrp2));

        when(playerStatsRepository.findById("p1")).thenReturn(Optional.of(stats(
                "p1", "20", "5", "5", "1", "1", "2", "0.40", "0.60", "4"
        )));
        when(playerStatsRepository.findById("p2")).thenReturn(Optional.of(stats(
                "p2", "10", "10", "10", "2", "2", "4", "0.50", "0.80", "2"
        )));

        InsightsDto dto = service.computeInsights("l1");

        assertEquals(new BigDecimal("20.000"), dto.myAverages().get("pts"));

        Set<String> strongestCats = dto.strongest().stream()
                .map(CategoryRankDto::category)
                .collect(Collectors.toSet());

        assertEquals(Set.of("pts", "tov", "fg3"), strongestCats);

        CategoryRankDto tovRank = dto.strongest().stream()
                .filter(r -> r.category().equals("tov"))
                .findFirst()
                .orElseThrow();

        assertEquals(1, tovRank.rank());
    }

    private PlayerStats stats(
            String id,
            String pts,
            String reb,
            String ast,
            String stl,
            String blk,
            String tov,
            String fgPct,
            String ftPct,
            String fg3
    ) {
        PlayerStats ps = new PlayerStats();
        ps.setId(id);
        ps.setPtsPg(new BigDecimal(pts));
        ps.setRebPg(new BigDecimal(reb));
        ps.setAstPg(new BigDecimal(ast));
        ps.setStlPg(new BigDecimal(stl));
        ps.setBlkPg(new BigDecimal(blk));
        ps.setTovPg(new BigDecimal(tov));
        ps.setFgPct(new BigDecimal(fgPct));
        ps.setFtPct(new BigDecimal(ftPct));
        ps.setFg3MadePg(new BigDecimal(fg3));
        return ps;
    }
}
