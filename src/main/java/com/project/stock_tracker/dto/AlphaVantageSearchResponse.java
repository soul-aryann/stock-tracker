package com.project.stock_tracker.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AlphaVantageSearchResponse(
    @JsonProperty("bestMatches")
    List<Match> bestMatches
) {
    public record Match(
        @JsonProperty("1. symbol") String symbol,
        @JsonProperty("2. name") String name,
        @JsonProperty("3. type") String type,
        @JsonProperty("4. region") String region,
        @JsonProperty("8. currency") String currency
    ) {}
}
