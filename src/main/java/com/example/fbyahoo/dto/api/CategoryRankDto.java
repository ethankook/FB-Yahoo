package com.example.fbyahoo.dto.api;

import java.math.BigDecimal;

public record CategoryRankDto(
        String category,
        BigDecimal myValue,
        BigDecimal leagueAvg,
        int rank,
        int totalTeams
) {}
