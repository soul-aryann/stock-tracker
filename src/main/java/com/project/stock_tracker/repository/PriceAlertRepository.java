package com.project.stock_tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.stock_tracker.entity.PriceAlert;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findByTriggeredFalse();
    List<PriceAlert> findByStockSymbol(String stockSymbol);
}
