package com.project.stock_tracker.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.project.stock_tracker.dto.AlphaVantageResponse;
import com.project.stock_tracker.dto.AlphaVantageSearchResponse;
import com.project.stock_tracker.dto.StockHistoryResponse;
import com.project.stock_tracker.dto.StockOverviewResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockClient { 
    private final WebClient webClient;

    @Value("${alpha.vantage.api.key}")
    private String apiKey;

    public AlphaVantageResponse getStockQuote(String stockSymbol) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("function", "GLOBAL_QUOTE")
                        .queryParam("symbol", stockSymbol)
                        .queryParam("apikey", getCleanKey())
                        .build())
                .retrieve()
                .bodyToMono(AlphaVantageResponse.class)
                .block();
    } 

    public StockOverviewResponse getStockOverview(String stockSymbol) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("function", "OVERVIEW")
                        .queryParam("symbol", stockSymbol)
                        .queryParam("apikey", getCleanKey())
                        .build())
                .retrieve()
                .bodyToMono(StockOverviewResponse.class)
                .block();
    }

    public StockHistoryResponse getStockHistory(String stockSymbol) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("function", "TIME_SERIES_DAILY")
                        .queryParam("symbol", stockSymbol)
                        .queryParam("apikey", getCleanKey())
                        .build())
                .retrieve()
                .bodyToMono(StockHistoryResponse.class)
                .block();
    }

    public AlphaVantageSearchResponse searchSymbols(String keywords) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("function", "SYMBOL_SEARCH")
                        .queryParam("keywords", keywords)
                        .queryParam("apikey", getCleanKey())
                        .build())
                .retrieve()
                .bodyToMono(AlphaVantageSearchResponse.class)
                .block();
    }

    private String getCleanKey() {
        return apiKey != null ? apiKey.trim() : "";
    }
}
