package com.example.fbyahoo.dto.api;

import java.math.BigDecimal;

public record StandingDto(
        String teamKey,
        String name,
        String logoUrl,
        Integer rank,
        Integer wins,
        Integer losses,
        Integer ties,
        BigDecimal pct,
        BigDecimal gamesBack,
        BigDecimal pointsFor,
        BigDecimal pointsAgainst,
        boolean isMyTeam
) {}
