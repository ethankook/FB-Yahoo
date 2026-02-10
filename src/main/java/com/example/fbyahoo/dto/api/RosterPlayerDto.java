package com.example.fbyahoo.dto.api;

public record RosterPlayerDto(
        String playerId,
        String name,
        String displayPosition,
        String[] eligiblePositions,
        String editorialTeamAbbr,
        String headshotUrl,
        String status,
        String injuryNote,
        PlayerStatsDto stats
) {}
