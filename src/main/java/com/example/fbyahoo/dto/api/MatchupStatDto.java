package com.example.fbyahoo.dto.api;

import java.math.BigDecimal;

public record MatchupStatDto(
        String statName,
        BigDecimal myValue,
        BigDecimal opponentValue,
        String winner
) {}
