package com.project.stock_tracker.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummaryResponse {
    private double totalInvested;
    private double currentValue;
    private double totalProfitLoss;
    private double totalReturnPercentage;
    private List<PortfolioHoldingResponse> holdings;
}
