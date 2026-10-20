 package com.project.stock_tracker.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.project.stock_tracker.dto.DailyStockResponse;
import com.project.stock_tracker.dto.StockOverviewResponse;
import com.project.stock_tracker.dto.StockResponse;
import com.project.stock_tracker.dto.SymbolSearchResult;
import com.project.stock_tracker.entity.FavouriteStock;
import com.project.stock_tracker.service.StockService;
import org.springframework.http.ResponseEntity;
import com.project.stock_tracker.dto.FavouriteStockRequest;

@RestController
@RequestMapping("/api/v1/stocks")
public class StockController {
    private final StockService stockService;


    public StockController(final StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/search")
    public List<SymbolSearchResult> searchStocks(@RequestParam String query) {
        return stockService.searchSymbols(query);
    }

    @GetMapping("/{stockSymbol}") 
    public StockResponse getStockInfo(@PathVariable String stockSymbol) {
        return stockService.getStockForSymbol (stockSymbol.toUpperCase());
    }

    @GetMapping("/{stockSymbol}/overview")
    public StockOverviewResponse getStockOverview(@PathVariable String stockSymbol) {
        return stockService.getStockOverviewForSymbol(stockSymbol.toUpperCase()) ;
    }

    @GetMapping("/{symbol}/history")
    public List<DailyStockResponse> getStockHistory(@PathVariable("symbol") final String stockSymbol,
            @RequestParam(defaultValue = "30") final int days) {
        return stockService.getHistory(stockSymbol.toUpperCase(), days);
    }

    @PostMapping("/favourites")
    public ResponseEntity<FavouriteStock> saveFavouriteStock(@RequestBody FavouriteStockRequest request) {
        final FavouriteStock saved = stockService.addFavourite(request.getSymbol());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/favourites/{symbol}")
    public ResponseEntity<Void> deleteFavouriteStock(@PathVariable String symbol) {
        stockService.removeFavourite(symbol.toUpperCase());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favourites")
    public List<StockResponse> getFavouriteWithPrices() { 
        return stockService.getFavouritesWithPrices();
        
    } 
}
