package com.project.stock_tracker.exception;

public class StockDataUnavailableException extends RuntimeException {
    public StockDataUnavailableException(String message) {
        super(message);
    }
}
