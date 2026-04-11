package com.project.stock_tracker.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder; 

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavouriteStockRequest {
    private String symbol;
}
