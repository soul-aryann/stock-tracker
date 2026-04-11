package com.project.stock_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StockOverviewResponse(
    @JsonProperty("Symbol") String symbol,
    @JsonProperty("Name") String name,   
    @JsonProperty("Description") String description,
    @JsonProperty("Sector") String sector,
    @JsonProperty("Industry") String industry,
    @JsonProperty("Country") String country,
    @JsonProperty("Currency") String currency,
    @JsonProperty("MarketCapitalization") String marketCapitalization
) {

}
