package com.example.fbyahoo.service;

import com.example.fbyahoo.dto.api.CategoryRankDto;
import com.example.fbyahoo.dto.api.InsightsDto;
import com.example.fbyahoo.model.LeagueRosteredPlayer;
import com.example.fbyahoo.model.PlayerStats;
import com.example.fbyahoo.model.Team;
import com.example.fbyahoo.repo.LeagueRosteredPlayerRepository;
import com.example.fbyahoo.repo.PlayerStatsRepository;
import com.example.fbyahoo.repo.TeamRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LeagueAnalyticsService {

    private final TeamRepository teamRepository;
    private final LeagueRosteredPlayerRepository lrpRepository;
    private final PlayerStatsRepository playerStatsRepository;

    private static final List<String> CATEGORIES = List.of(
            "pts", "reb", "ast", "stl", "blk", "tov", "fgPct", "ftPct", "fg3"
    );

    public LeagueAnalyticsService(
            TeamRepository teamRepository,
            LeagueRosteredPlayerRepository lrpRepository,
            PlayerStatsRepository playerStatsRepository
    ) {
        this.teamRepository = teamRepository;
        this.lrpRepository = lrpRepository;
        this.playerStatsRepository = playerStatsRepository;
    }

    public InsightsDto computeInsights(String leagueKey) {
        List<Team> teams = teamRepository.findByLeague_LeagueKey(leagueKey);

        Team myTeam = teams.stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsOwnedByCurrentLogin()))
                .findFirst()
                .orElse(null);

        if (myTeam == null) {
            return new InsightsDto(Map.of(), List.of(), List.of());
        }

        // Compute averages per team per category
        Map<String, Map<String, BigDecimal>> teamAverages = new LinkedHashMap<>();

        for (Team team : teams) {
            List<LeagueRosteredPlayer> roster = lrpRepository.findByTeam_TeamKey(team.getTeamKey());
            Map<String, BigDecimal> avgs = computeTeamAverages(roster);
            teamAverages.put(team.getTeamKey(), avgs);
        }

        Map<String, BigDecimal> myAvgs = teamAverages.getOrDefault(myTeam.getTeamKey(), Map.of());

        // Compute league-wide average per category
        Map<String, BigDecimal> leagueAvgs = new LinkedHashMap<>();
        for (String cat : CATEGORIES) {
            BigDecimal sum = BigDecimal.ZERO;
            int count = 0;
            for (Map<String, BigDecimal> ta : teamAverages.values()) {
                BigDecimal val = ta.get(cat);
                if (val != null) {
                    sum = sum.add(val);
                    count++;
                }
            }
            leagueAvgs.put(cat, count > 0 ? sum.divide(BigDecimal.valueOf(count), 3, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        }

        // Rank my team per category
        List<CategoryRankDto> allRanks = new ArrayList<>();
        int totalTeams = teams.size();

        for (String cat : CATEGORIES) {
            BigDecimal myVal = myAvgs.getOrDefault(cat, BigDecimal.ZERO);

            // For turnovers, lower is better
            boolean lowerIsBetter = "tov".equals(cat);

            List<BigDecimal> allValues = teamAverages.values().stream()
                    .map(m -> m.getOrDefault(cat, BigDecimal.ZERO))
                    .sorted(lowerIsBetter ? Comparator.naturalOrder() : Comparator.reverseOrder())
                    .toList();

            int rank = 1;
            for (BigDecimal val : allValues) {
                if (val.equals(myVal)) break;
                rank++;
            }

            allRanks.add(new CategoryRankDto(cat, myVal, leagueAvgs.get(cat), rank, totalTeams));
        }

        // Top 3 strongest (lowest rank = best)
        List<CategoryRankDto> strongest = allRanks.stream()
                .sorted(Comparator.comparingInt(CategoryRankDto::rank))
                .limit(3)
                .toList();

        // Bottom 3 weakest (highest rank = worst)
        List<CategoryRankDto> weakest = allRanks.stream()
                .sorted(Comparator.comparingInt(CategoryRankDto::rank).reversed())
                .limit(3)
                .toList();

        return new InsightsDto(myAvgs, strongest, weakest);
    }

    private Map<String, BigDecimal> computeTeamAverages(List<LeagueRosteredPlayer> roster) {
        List<PlayerStats> statsList = roster.stream()
                .map(lrp -> playerStatsRepository.findById(lrp.getPlayer().getPlayerId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        if (statsList.isEmpty()) {
            return Map.of();
        }

        Map<String, BigDecimal> averages = new LinkedHashMap<>();
        int count = statsList.size();
        BigDecimal divisor = BigDecimal.valueOf(count);

        averages.put("pts", avg(statsList, PlayerStats::getPtsPg, divisor));
        averages.put("reb", avg(statsList, PlayerStats::getRebPg, divisor));
        averages.put("ast", avg(statsList, PlayerStats::getAstPg, divisor));
        averages.put("stl", avg(statsList, PlayerStats::getStlPg, divisor));
        averages.put("blk", avg(statsList, PlayerStats::getBlkPg, divisor));
        averages.put("tov", avg(statsList, PlayerStats::getTovPg, divisor));
        averages.put("fgPct", avg(statsList, PlayerStats::getFgPct, divisor));
        averages.put("ftPct", avg(statsList, PlayerStats::getFtPct, divisor));
        averages.put("fg3", avg(statsList, PlayerStats::getFg3MadePg, divisor));

        return averages;
    }

    private BigDecimal avg(List<PlayerStats> stats, Function<PlayerStats, BigDecimal> getter, BigDecimal divisor) {
        BigDecimal sum = BigDecimal.ZERO;
        for (PlayerStats ps : stats) {
            BigDecimal val = getter.apply(ps);
            if (val != null) sum = sum.add(val);
        }
        return sum.divide(divisor, 3, RoundingMode.HALF_UP);
    }
}
