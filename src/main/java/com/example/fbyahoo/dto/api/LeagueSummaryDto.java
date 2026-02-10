package com.example.fbyahoo.dto.api;

public record LeagueSummaryDto(
        String leagueKey,
        String name,
        Integer season,
        String scoringType,
        Integer numTeams,
        Integer currentWeek,
        Integer matchupWeek,
        String logoUrl
) {}
