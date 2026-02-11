package com.example.fbyahoo.dto.api;

public record AvailablePlayerDto(
        String playerId,
        String name,
        String displayPosition,
        String[] eligiblePositions,
        String editorialTeamAbbr,
        String headshotUrl,
        String status,
        PlayerStatsDto stats,
        Integer percentOwned,
        Integer deltaWeek
) {}
