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
public class PortfolioHoldingRequest {
    private String symbol;
    private double quantity;
    private double buyPrice;
    private LocalDate buyDate;
}
