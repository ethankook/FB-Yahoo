package com.example.fbyahoo.dto.api;

import java.math.BigDecimal;

public record PlayerStatsDto(
        BigDecimal ptsPg,
        BigDecimal rebPg,
        BigDecimal astPg,
        BigDecimal stlPg,
        BigDecimal blkPg,
        BigDecimal tovPg,
        BigDecimal fgPct,
        BigDecimal ftPct,
        BigDecimal fg3MadePg,
        Integer gamesPlayed
) {}
