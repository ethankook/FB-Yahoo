package com.example.fbyahoo.controller.api;

import com.example.fbyahoo.dto.api.*;
import com.example.fbyahoo.model.*;
import com.example.fbyahoo.repo.*;
import com.example.fbyahoo.service.LeagueAnalyticsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/leagues")
public class LeagueApiController {

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final LeagueRosteredPlayerRepository lrpRepository;
    private final PlayerStatsRepository playerStatsRepository;
    private final PlayerOwnershipRepository playerOwnershipRepository;
    private final PlayerRepository playerRepository;
    private final MatchupRepository matchupRepository;
    private final LeagueAnalyticsService leagueAnalyticsService;

    public LeagueApiController(
            LeagueRepository leagueRepository,
            TeamRepository teamRepository,
            LeagueRosteredPlayerRepository lrpRepository,
            PlayerStatsRepository playerStatsRepository,
            PlayerOwnershipRepository playerOwnershipRepository,
            PlayerRepository playerRepository,
            MatchupRepository matchupRepository,
            LeagueAnalyticsService leagueAnalyticsService
    ) {
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
        this.lrpRepository = lrpRepository;
        this.playerStatsRepository = playerStatsRepository;
        this.playerOwnershipRepository = playerOwnershipRepository;
        this.playerRepository = playerRepository;
        this.matchupRepository = matchupRepository;
        this.leagueAnalyticsService = leagueAnalyticsService;
    }

    @GetMapping
    public List<LeagueSummaryDto> listLeagues() {
        return leagueRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    @GetMapping("/{leagueKey}")
    public ResponseEntity<LeagueDetailDto> getLeague(@PathVariable String leagueKey) {
        return leagueRepository.findById(leagueKey)
                .map(league -> {
                    TeamDto myTeam = teamRepository.findByLeague_LeagueKey(leagueKey).stream()
                            .filter(t -> Boolean.TRUE.equals(t.getIsOwnedByCurrentLogin()))
                            .findFirst()
                            .map(this::toTeamDto)
                            .orElse(null);
                    return ResponseEntity.ok(new LeagueDetailDto(toSummary(league), myTeam));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{leagueKey}/roster")
    public ResponseEntity<List<RosterPlayerDto>> getRoster(@PathVariable String leagueKey) {
        Optional<Team> myTeam = teamRepository.findByLeague_LeagueKey(leagueKey).stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsOwnedByCurrentLogin()))
                .findFirst();

        if (myTeam.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<RosterPlayerDto> roster = lrpRepository.findByTeam_TeamKey(myTeam.get().getTeamKey()).stream()
                .map(lrp -> {
                    Player player = lrp.getPlayer();
                    PlayerStats stats = playerStatsRepository.findById(player.getPlayerId()).orElse(null);
                    return new RosterPlayerDto(
                            player.getPlayerId(),
                            player.getNameFull(),
                            player.getDisplayPosition(),
                            player.getEligiblePositions(),
                            player.getEditorialTeamAbbr(),
                            player.getHeadshotUrl(),
                            player.getStatus(),
                            player.getInjuryNote(),
                            stats != null ? toStatsDto(stats) : null
                    );
                })
                .toList();

        return ResponseEntity.ok(roster);
    }

    @GetMapping("/{leagueKey}/available")
    public List<AvailablePlayerDto> getAvailablePlayers(
            @PathVariable String leagueKey,
            @RequestParam(defaultValue = "pts") String category,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest page = PageRequest.of(0, limit);

        List<PlayerStats> statsList = switch (category) {
            case "pts" -> playerStatsRepository.topAvailableByPtsPg(leagueKey, page);
            case "reb" -> playerStatsRepository.topAvailableByRebPg(leagueKey, page);
            case "ast" -> playerStatsRepository.topAvailableByAstPg(leagueKey, page);
            case "stl" -> playerStatsRepository.topAvailableByStlPg(leagueKey, page);
            case "blk" -> playerStatsRepository.topAvailableByBlkPg(leagueKey, page);
            case "tov" -> playerStatsRepository.topAvailableByLowTovPg(leagueKey, page);
            case "fg_pct" -> playerStatsRepository.topAvailableByFgPct(leagueKey, page);
            case "ft_pct" -> playerStatsRepository.topAvailableByFtPct(leagueKey, page);
            case "fg3" -> playerStatsRepository.topAvailableByFg3MadePg(leagueKey, page);
            default -> playerStatsRepository.topAvailableByPtsPg(leagueKey, page);
        };

        return statsList.stream()
                .map(ps -> {
                    Player player = ps.getPlayer();
                    if (player == null) {
                        player = playerRepository.findById(ps.getId()).orElse(null);
                    }
                    Integer percentOwned = playerOwnershipRepository
                            .findByIdPlayerIdAndIdSeason(ps.getId(), 2025)
                            .map(PlayerOwnership::getPercentOwned)
                            .orElse(null);
                    return new AvailablePlayerDto(
                            ps.getId(),
                            player != null ? player.getNameFull() : null,
                            player != null ? player.getDisplayPosition() : null,
                            player != null ? player.getEligiblePositions() : null,
                            player != null ? player.getEditorialTeamAbbr() : null,
                            player != null ? player.getHeadshotUrl() : null,
                            player != null ? player.getStatus() : null,
                            toStatsDto(ps),
                            percentOwned
                    );
                })
                .toList();
    }

    @GetMapping("/{leagueKey}/standings")
    public ResponseEntity<List<StandingDto>> getStandings(@PathVariable String leagueKey) {
        if (leagueRepository.findById(leagueKey).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<StandingDto> standings = teamRepository.findByLeague_LeagueKey(leagueKey).stream()
                .sorted((a, b) -> {
                    Integer ra = a.getStandingRank();
                    Integer rb = b.getStandingRank();
                    if (ra == null && rb == null) return 0;
                    if (ra == null) return 1;
                    if (rb == null) return -1;
                    return ra.compareTo(rb);
                })
                .map(t -> new StandingDto(
                        t.getTeamKey(),
                        t.getName(),
                        t.getLogoUrl(),
                        t.getStandingRank(),
                        t.getStandingWins(),
                        t.getStandingLosses(),
                        t.getStandingTies(),
                        t.getStandingPct(),
                        t.getStandingGamesBack(),
                        t.getStandingPointsFor(),
                        t.getStandingPointsAgainst(),
                        Boolean.TRUE.equals(t.getIsOwnedByCurrentLogin())
                ))
                .toList();

        return ResponseEntity.ok(standings);
    }

    @GetMapping("/{leagueKey}/matchup")
    public ResponseEntity<MatchupDto> getMatchup(
            @PathVariable String leagueKey,
            @RequestParam(required = false) Integer week
    ) {
        League league = leagueRepository.findById(leagueKey).orElse(null);
        if (league == null) {
            return ResponseEntity.notFound().build();
        }

        int targetWeek = week != null ? week : (league.getCurrentWeek() != null ? league.getCurrentWeek() : 1);

        Optional<Team> myTeamOpt = teamRepository.findByLeague_LeagueKey(leagueKey).stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsOwnedByCurrentLogin()))
                .findFirst();

        if (myTeamOpt.isEmpty()) {
            return ResponseEntity.ok(null);
        }

        Team myTeam = myTeamOpt.get();
        String myKey = myTeam.getTeamKey();

        Optional<Matchup> matchupOpt = matchupRepository.findByLeagueKeyAndWeek(leagueKey, targetWeek).stream()
                .filter(m -> myKey.equals(m.getTeam1Key()) || myKey.equals(m.getTeam2Key()))
                .findFirst();

        if (matchupOpt.isEmpty()) {
            return ResponseEntity.ok(null);
        }

        Matchup matchup = matchupOpt.get();
        boolean isTeam1 = myKey.equals(matchup.getTeam1Key());
        String opponentKey = isTeam1 ? matchup.getTeam2Key() : matchup.getTeam1Key();
        Team opponent = teamRepository.findById(opponentKey).orElse(null);

        List<MatchupStatDto> statDtos = matchup.getStats().stream()
                .map(ms -> {
                    var myVal = isTeam1 ? ms.getTeam1Value() : ms.getTeam2Value();
                    var oppVal = isTeam1 ? ms.getTeam2Value() : ms.getTeam1Value();
                    String winner;
                    if (Boolean.TRUE.equals(ms.getIsTied())) {
                        winner = "tied";
                    } else if (myKey.equals(ms.getWinnerTeamKey())) {
                        winner = "me";
                    } else {
                        winner = "opponent";
                    }
                    return new MatchupStatDto(ms.getStatName(), myVal, oppVal, winner);
                })
                .toList();

        int myWins = (int) statDtos.stream().filter(s -> "me".equals(s.winner())).count();
        int oppWins = (int) statDtos.stream().filter(s -> "opponent".equals(s.winner())).count();
        int ties = (int) statDtos.stream().filter(s -> "tied".equals(s.winner())).count();

        return ResponseEntity.ok(new MatchupDto(
                targetWeek,
                toTeamDto(myTeam),
                opponent != null ? toTeamDto(opponent) : null,
                statDtos,
                myWins,
                oppWins,
                ties
        ));
    }

    @GetMapping("/{leagueKey}/insights")
    public ResponseEntity<InsightsDto> getInsights(@PathVariable String leagueKey) {
        if (leagueRepository.findById(leagueKey).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(leagueAnalyticsService.computeInsights(leagueKey));
    }

    private LeagueSummaryDto toSummary(League l) {
        return new LeagueSummaryDto(
                l.getLeagueKey(), l.getName(), l.getSeason(), l.getScoringType(),
                l.getNumTeams(), l.getCurrentWeek(), l.getMatchupWeek(), l.getLogoUrl()
        );
    }

    private TeamDto toTeamDto(Team t) {
        return new TeamDto(
                t.getTeamKey(), t.getName(), t.getLogoUrl(), t.getManagerNickname(),
                t.getWaiverPriority(), t.getNumberOfMoves(), t.getNumberOfTrades(),
                t.getStandingRank(), t.getStandingWins(), t.getStandingLosses(),
                t.getStandingTies(), t.getStandingPct()
        );
    }

    private PlayerStatsDto toStatsDto(PlayerStats ps) {
        return new PlayerStatsDto(
                ps.getPtsPg(), ps.getRebPg(), ps.getAstPg(), ps.getStlPg(),
                ps.getBlkPg(), ps.getTovPg(), ps.getFgPct(), ps.getFtPct(),
                ps.getFg3MadePg(), ps.getGamesPlayed()
        );
    }
}
