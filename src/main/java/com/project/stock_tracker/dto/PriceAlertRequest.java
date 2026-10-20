package com.project.stock_tracker.dto;

import com.project.stock_tracker.entity.AlertDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlertRequest {
    private String symbol;
    private double targetPrice;
    private AlertDirection direction;
}
