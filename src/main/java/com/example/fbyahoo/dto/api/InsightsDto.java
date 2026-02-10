package com.example.fbyahoo.dto.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record InsightsDto(
        Map<String, BigDecimal> myAverages,
        List<CategoryRankDto> strongest,
        List<CategoryRankDto> weakest
) {}
