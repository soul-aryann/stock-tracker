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
import com.project.stock_tracker.entity.FavouriteStock;
import com.project.stock_tracker.exception.FavouriteAlreadyExistsException;
import com.project.stock_tracker.repository.FavouriteStockRepository;
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
        return StockResponse.builder()
                .symbol(response.globalQuote().symbol())
                .price(Double.parseDouble(response.globalQuote().price()))
                .lastUpdated(response.globalQuote().latestTradingDay())
                .build();  
    }
    
    public StockOverviewResponse getStockOverviewForSymbol(final String stockSymbol) {
        return stockClient.getStockOverview(stockSymbol);
    }

    public List<DailyStockResponse> getHistory(final String stockSymbol, final int days) {
        StockHistoryResponse stockHistoryResponse = stockClient.getStockHistory(stockSymbol);
        return stockHistoryResponse.dailyStock().entrySet().stream()
                .limit(days)
                .map(entry -> {var date =entry.getKey();
                    var daily = entry.getValue();
                    return new DailyStockResponse(
                        date,
                            Double.parseDouble(daily.open()),
                            Double.parseDouble(daily.high()),
                            Double.parseDouble(daily.low()),
                            Double.parseDouble(daily.close()),
                            Long.parseLong(daily.volume())
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

    public List<StockResponse> getFavouritesWithPrices() {
        List<FavouriteStock> favourites = favouriteStockRepository.findAll();
        return favourites.stream()
                .map(fav -> getStockForSymbol(fav.getStockSymbol()))
                .collect(Collectors.toList());
    }

}
 