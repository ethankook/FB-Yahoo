package com.example.fbyahoo.dto.api;

import java.util.List;

public record MatchupDto(
        Integer week,
        TeamDto myTeam,
        TeamDto opponent,
        List<MatchupStatDto> stats,
        int myWins,
        int opponentWins,
        int ties
) {}
