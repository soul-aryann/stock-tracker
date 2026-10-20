package com.project.stock_tracker.dto;

public record SymbolSearchResult(
    String symbol,
    String name,
    String type,
    String region,
    String currency
) {}
