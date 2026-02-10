package com.example.fbyahoo.dto.api;

import java.math.BigDecimal;

public record TeamDto(
        String teamKey,
        String name,
        String logoUrl,
        String managerNickname,
        Integer waiverPriority,
        Integer numberOfMoves,
        Integer numberOfTrades,
        Integer rank,
        Integer wins,
        Integer losses,
        Integer ties,
        BigDecimal pct
) {}
