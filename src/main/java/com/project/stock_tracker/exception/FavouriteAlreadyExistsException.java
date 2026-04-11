package com.project.stock_tracker.exception;

public class FavouriteAlreadyExistsException extends RuntimeException {
    public FavouriteAlreadyExistsException(String symbol) {
        super("Favourite stock with symbol " + symbol + " already exists.");
    }  

}
