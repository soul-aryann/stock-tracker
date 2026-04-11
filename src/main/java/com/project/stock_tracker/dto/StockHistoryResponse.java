package com.project.stock_tracker.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StockHistoryResponse(
    @JsonProperty("Meta Data") MetaData metaData,
    @JsonProperty("Time Series (Daily)") Map<String, DailyHistoryEntry> dailyStock
) {
    public record MetaData(
        @JsonProperty("2. Symbol") String symbol

    ) {}

    public record DailyHistoryEntry(
        @JsonProperty("1. open") String open,
        @JsonProperty("2. high") String high,
        @JsonProperty("3. low") String low,
        @JsonProperty("4. close") String close,
        @JsonProperty("5. volume") String volume
    ) {}
}
