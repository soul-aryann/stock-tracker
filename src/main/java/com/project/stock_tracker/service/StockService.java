package com.project.stock_tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.stock_tracker.client.StockClient;
import com.project.stock_tracker.dto.AlphaVantageResponse;
import com.project.stock_tracker.dto.DailyStockResponse;
import com.project.stock_tracker.dto.StockHistoryResponse;
import com.project.stock_tracker.dto.StockOverviewResponse;
import com.project.stock_tracker.dto.StockResponse;
import com.project.stock_tracker.dto.SymbolSearchResult;
import com.project.stock_tracker.entity.FavouriteStock;
import com.project.stock_tracker.exception.FavouriteAlreadyExistsException;
import com.project.stock_tracker.exception.FavouriteNotFoundException;
import com.project.stock_tracker.exception.StockDataUnavailableException;
import com.project.stock_tracker.repository.FavouriteStockRepository;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;

@Service 
public class StockService { 
    private  StockClient stockClient;
    private  FavouriteStockRepository favouriteStockRepository; 

    public StockService(final StockClient stockClient, final FavouriteStockRepository favouriteStockRepository) {
        this.stockClient = stockClient;
        this.favouriteStockRepository = favouriteStockRepository;
    }

    @Cacheable(value = "stocks", key ="#stockSymbol")
    public StockResponse getStockForSymbol(final String stockSymbol) {
        final AlphaVantageResponse response = stockClient.getStockQuote(stockSymbol);
        if (response == null || response.globalQuote() == null || response.globalQuote().symbol() == null) {
            String reason = "Stock data unavailable for " + stockSymbol;
            if (response != null) {
                if (response.note() != null) {
                    reason = "Alpha Vantage rate limit reached: " + response.note();
                } else if (response.information() != null) {
                    reason = "Alpha Vantage info: " + response.information();
                } else if (response.errorMessage() != null) {
                    reason = "Alpha Vantage error: " + response.errorMessage();
                }
            }
            throw new StockDataUnavailableException(reason);
        }
        var gq = response.globalQuote();
        return StockResponse.builder()
                .symbol(gq.symbol())
                .price(parseDoubleSafe(gq.price()))
                .open(parseDoubleSafe(gq.open()))
                .high(parseDoubleSafe(gq.high()))
                .low(parseDoubleSafe(gq.low()))
                .volume(parseLongSafe(gq.volume()))
                .previousClose(parseDoubleSafe(gq.previousClose()))
                .change(parseDoubleSafe(gq.change()))
                .changePercent(gq.changePercent() != null ? gq.changePercent().trim() : "0.0%")
                .lastUpdated(gq.latestTradingDay())
                .build();  
    }

    private double parseDoubleSafe(String val) {
        if (val == null || val.isBlank()) return 0.0;
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private long parseLongSafe(String val) {
        if (val == null || val.isBlank()) return 0L;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    @Cacheable(value = "overviews", key = "#stockSymbol")
    public StockOverviewResponse getStockOverviewForSymbol(final String stockSymbol) {
        return stockClient.getStockOverview(stockSymbol);
    }

    @Cacheable(value = "histories", key = "#stockSymbol + '_' + #days")
    public List<DailyStockResponse> getHistory(final String stockSymbol, final int days) {
        StockHistoryResponse stockHistoryResponse = stockClient.getStockHistory(stockSymbol);
        if (stockHistoryResponse == null || stockHistoryResponse.dailyStock() == null) {
            return List.of();
        }
        return stockHistoryResponse.dailyStock().entrySet().stream()
                .limit(days)
                .map(entry -> {
                    var date = entry.getKey();
                    var daily = entry.getValue();
                    return new DailyStockResponse(
                        date,
                        parseDoubleSafe(daily.open()),
                        parseDoubleSafe(daily.high()),
                        parseDoubleSafe(daily.low()),
                        parseDoubleSafe(daily.close()),
                        parseLongSafe(daily.volume())
                    );
                })
                .collect(Collectors.toList());
    }  

    @Transactional 
    public FavouriteStock addFavourite(final String stockSymbol) {
        if (favouriteStockRepository.existsByStockSymbol(stockSymbol)) {
            throw new FavouriteAlreadyExistsException(stockSymbol);
        }
        FavouriteStock favouriteStock = FavouriteStock.builder()
                .stockSymbol(stockSymbol)
                .build();
        return favouriteStockRepository.save(favouriteStock);
    }

    @Transactional
    public void removeFavourite(final String stockSymbol) {
        if (!favouriteStockRepository.existsByStockSymbol(stockSymbol)) {
            throw new FavouriteNotFoundException(stockSymbol);
        }
        favouriteStockRepository.deleteByStockSymbol(stockSymbol);
    }

    public List<StockResponse> getFavouritesWithPrices() {
        List<FavouriteStock> favourites = favouriteStockRepository.findAll();
        List<CompletableFuture<StockResponse>> futures = favourites.stream()
                .map(fav -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return getStockForSymbol(fav.getStockSymbol());
                    } catch (Exception e) {
                        return StockResponse.builder()
                                .symbol(fav.getStockSymbol())
                                .price(0.0)
                                .lastUpdated("Unavailable")
                                .build();
                    }
                }))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    public List<SymbolSearchResult> searchSymbols(final String keywords) {
        var response = stockClient.searchSymbols(keywords);
        if (response == null || response.bestMatches() == null) {
            return List.of();
        }
        return response.bestMatches().stream()
                .map(match -> new SymbolSearchResult(
                        match.symbol(),
                        match.name(),
                        match.type(),
                        match.region(),
                        match.currency()
                ))
                .collect(Collectors.toList());
    }

}
 