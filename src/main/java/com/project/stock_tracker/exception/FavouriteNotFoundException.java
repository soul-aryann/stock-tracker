package com.project.stock_tracker.exception;

public class FavouriteNotFoundException extends RuntimeException {
    public FavouriteNotFoundException(String symbol) {
        super("Favourite stock with symbol " + symbol + " not found.");
    }
}
