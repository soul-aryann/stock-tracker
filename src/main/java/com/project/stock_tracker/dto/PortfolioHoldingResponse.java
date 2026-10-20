package com.project.stock_tracker.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioHoldingResponse {
    private Long id;
    private String symbol;
    private double quantity;
    private double buyPrice;
    private LocalDate buyDate;
    private double currentPrice;
    private double totalInvested;
    private double currentValue;
    private double unrealizedProfitLoss;
    private double returnPercentage;
}
