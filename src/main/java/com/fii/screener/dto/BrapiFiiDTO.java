package com.fii.screener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BrapiFiiDTO(
        String symbol,
        Double price,
        Double dividendYield12m,
        Double priceToNav,
        Double vacancy,
        Double netWorth,
        Double equityValue,
        String segment,
        String type
) {
}
