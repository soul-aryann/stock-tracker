package com.project.stock_tracker.dto;

import lombok.Builder;

@Builder
public record StockResponse(
    String symbol,
    double price,
    double open,
    double high,
    double low,
    long volume,
    double previousClose,
    double change,
    String changePercent,
    String lastUpdated
) {
}