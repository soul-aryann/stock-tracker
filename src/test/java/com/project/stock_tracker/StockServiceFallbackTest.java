package com.project.stock_tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.stock_tracker.client.StockClient;
import com.project.stock_tracker.dto.DailyStockResponse;
import com.project.stock_tracker.dto.StockOverviewResponse;
import com.project.stock_tracker.dto.StockResponse;
import com.project.stock_tracker.dto.SymbolSearchResult;
import com.project.stock_tracker.repository.FavouriteStockRepository;
import com.project.stock_tracker.service.StockService;

@ExtendWith(MockitoExtension.class)
class StockServiceFallbackTest {

    @Mock
    private StockClient stockClient;

    @Mock
    private FavouriteStockRepository favouriteStockRepository;

    private StockService stockService;

    @BeforeEach
    void setUp() {
        stockService = new StockService(stockClient, favouriteStockRepository);
    }

    @Test
    void shouldReturnFallbackQuoteWhenAlphaVantageFails() {
        when(stockClient.getStockQuote(anyString())).thenThrow(new RuntimeException("Rate limit"));

        StockResponse quote = stockService.getStockForSymbol("IBM");

        assertNotNull(quote);
        assertEquals("IBM", quote.symbol());
        assertTrue(quote.price() > 0.0);
        assertTrue(quote.volume() > 0L);
    }

    @Test
    void shouldReturnFallbackOverviewWhenAlphaVantageFails() {
        when(stockClient.getStockOverview(anyString())).thenThrow(new RuntimeException("Rate limit"));

        StockOverviewResponse overview = stockService.getStockOverviewForSymbol("IBM");

        assertNotNull(overview);
        assertEquals("IBM", overview.symbol());
        assertFalse(overview.name().isBlank());
        assertNotNull(overview.marketCapitalization());
    }

    @Test
    void shouldReturnFallbackHistoryWhenAlphaVantageFails() {
        when(stockClient.getStockHistory(anyString())).thenThrow(new RuntimeException("Rate limit"));

        List<DailyStockResponse> history = stockService.getHistory("IBM", 30);

        assertNotNull(history);
        assertEquals(30, history.size());
        assertTrue(history.get(0).getClose() > 0.0);
    }

    @Test
    void shouldReturnFallbackSearchWhenAlphaVantageFails() {
        when(stockClient.searchSymbols(anyString())).thenThrow(new RuntimeException("Rate limit"));

        List<SymbolSearchResult> results = stockService.searchSymbols("IBM");

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("IBM", results.get(0).symbol());
    }
}
