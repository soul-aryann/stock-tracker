package com.project.stock_tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(FavouriteAlreadyExistsException.class)
    public ResponseEntity<String> handleFavouriteAlreadyExistsException(FavouriteAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(FavouriteNotFoundException.class)
    public ResponseEntity<String> handleFavouriteNotFoundException(FavouriteNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(StockDataUnavailableException.class)
    public ResponseEntity<String> handleStockDataUnavailableException(StockDataUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }
}
