package com.project.stock_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.stock_tracker.entity.FavouriteStock;

@Repository
public interface FavouriteStockRepository extends JpaRepository<FavouriteStock, Long> {
    boolean existsByStockSymbol(String stockSymbol);
    void deleteByStockSymbol(String stockSymbol);
}
