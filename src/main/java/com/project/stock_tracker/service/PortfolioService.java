package com.project.stock_tracker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.stock_tracker.dto.PortfolioHoldingRequest;
import com.project.stock_tracker.dto.PortfolioHoldingResponse;
import com.project.stock_tracker.dto.PortfolioSummaryResponse;
import com.project.stock_tracker.dto.StockResponse;
import com.project.stock_tracker.entity.PortfolioHolding;
import com.project.stock_tracker.repository.PortfolioHoldingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioHoldingRepository portfolioHoldingRepository;
    private final StockService stockService;

    @Transactional
    public PortfolioHolding addHolding(final PortfolioHoldingRequest request) {
        PortfolioHolding holding = PortfolioHolding.builder()
                .stockSymbol(request.getSymbol().toUpperCase())
                .quantity(request.getQuantity())
                .buyPrice(request.getBuyPrice())
                .buyDate(request.getBuyDate() != null ? request.getBuyDate() : LocalDate.now())
                .build();
        return portfolioHoldingRepository.save(holding);
    }

    public List<PortfolioHoldingResponse> getAllHoldings() {
        List<PortfolioHolding> holdings = portfolioHoldingRepository.findAll();
        return holdings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PortfolioSummaryResponse getPortfolioSummary() {
        List<PortfolioHoldingResponse> holdingResponses = getAllHoldings();
        double totalInvested = holdingResponses.stream().mapToDouble(PortfolioHoldingResponse::getTotalInvested).sum();
        double currentValue = holdingResponses.stream().mapToDouble(PortfolioHoldingResponse::getCurrentValue).sum();
        double totalProfitLoss = currentValue - totalInvested;
        double totalReturnPercentage = totalInvested > 0 ? (totalProfitLoss / totalInvested) * 100.0 : 0.0;

        return PortfolioSummaryResponse.builder()
                .totalInvested(round(totalInvested))
                .currentValue(round(currentValue))
                .totalProfitLoss(round(totalProfitLoss))
                .totalReturnPercentage(round(totalReturnPercentage))
                .holdings(holdingResponses)
                .build();
    }

    @Transactional
    public void deleteHolding(final Long id) {
        portfolioHoldingRepository.deleteById(id);
    }

    private PortfolioHoldingResponse mapToResponse(PortfolioHolding holding) {
        double currentPrice = holding.getBuyPrice();
        try {
            StockResponse stock = stockService.getStockForSymbol(holding.getStockSymbol());
            if (stock != null) {
                currentPrice = stock.price();
            }
        } catch (Exception ignored) {
            // Fallback to buyPrice if external API is temporarily unavailable
        }

        double totalInvested = holding.getQuantity() * holding.getBuyPrice();
        double currentValue = holding.getQuantity() * currentPrice;
        double profitLoss = currentValue - totalInvested;
        double returnPercentage = totalInvested > 0 ? (profitLoss / totalInvested) * 100.0 : 0.0;

        return PortfolioHoldingResponse.builder()
                .id(holding.getId())
                .symbol(holding.getStockSymbol())
                .quantity(holding.getQuantity())
                .buyPrice(round(holding.getBuyPrice()))
                .buyDate(holding.getBuyDate())
                .currentPrice(round(currentPrice))
                .totalInvested(round(totalInvested))
                .currentValue(round(currentValue))
                .unrealizedProfitLoss(round(profitLoss))
                .returnPercentage(round(returnPercentage))
                .build();
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
