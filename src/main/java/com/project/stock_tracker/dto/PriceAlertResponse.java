package com.project.stock_tracker.dto;

import java.time.LocalDateTime;

import com.project.stock_tracker.entity.AlertDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlertResponse {
    private Long id;
    private String symbol;
    private double targetPrice;
    private AlertDirection direction;
    private boolean triggered;
    private double currentPrice;
    private String statusMessage;
    private LocalDateTime createdAt;
}
