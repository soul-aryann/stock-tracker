package com.project.stock_tracker.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.stock_tracker.dto.PriceAlertRequest;
import com.project.stock_tracker.dto.PriceAlertResponse;
import com.project.stock_tracker.dto.StockResponse;
import com.project.stock_tracker.entity.AlertDirection;
import com.project.stock_tracker.entity.PriceAlert;
import com.project.stock_tracker.repository.PriceAlertRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceAlertService {

    private final PriceAlertRepository priceAlertRepository;
    private final StockService stockService;

    @Transactional
    public PriceAlert createAlert(final PriceAlertRequest request) {
        PriceAlert alert = PriceAlert.builder()
                .stockSymbol(request.getSymbol().toUpperCase())
                .targetPrice(request.getTargetPrice())
                .direction(request.getDirection() != null ? request.getDirection() : AlertDirection.ABOVE)
                .triggered(false)
                .createdAt(LocalDateTime.now())
                .build();
        return priceAlertRepository.save(alert);
    }

    public List<PriceAlertResponse> getAllAlerts() {
        List<PriceAlert> alerts = priceAlertRepository.findAll();
        return alerts.stream()
                .map(this::evaluateAlert)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PriceAlertResponse> checkAndTriggerAlerts() {
        List<PriceAlert> pendingAlerts = priceAlertRepository.findByTriggeredFalse();
        for (PriceAlert alert : pendingAlerts) {
            try {
                StockResponse stock = stockService.getStockForSymbol(alert.getStockSymbol());
                if (stock != null) {
                    boolean triggered = false;
                    if (alert.getDirection() == AlertDirection.ABOVE && stock.price() >= alert.getTargetPrice()) {
                        triggered = true;
                    } else if (alert.getDirection() == AlertDirection.BELOW && stock.price() <= alert.getTargetPrice()) {
                        triggered = true;
                    }

                    if (triggered) {
                        alert.setTriggered(true);
                        priceAlertRepository.save(alert);
                    }
                }
            } catch (Exception ignored) {
                // If quote is unavailable, keep alert untriggered
            }
        }
        return getAllAlerts();
    }

    @Transactional
    public void deleteAlert(final Long id) {
        priceAlertRepository.deleteById(id);
    }

    private PriceAlertResponse evaluateAlert(PriceAlert alert) {
        double currentPrice = 0.0;
        String status = alert.isTriggered() ? "TRIGGERED" : "PENDING";
        try {
            StockResponse stock = stockService.getStockForSymbol(alert.getStockSymbol());
            if (stock != null) {
                currentPrice = stock.price();
            }
        } catch (Exception e) {
            status = "PRICE_UNAVAILABLE";
        }

        return PriceAlertResponse.builder()
                .id(alert.getId())
                .symbol(alert.getStockSymbol())
                .targetPrice(alert.getTargetPrice())
                .direction(alert.getDirection())
                .triggered(alert.isTriggered())
                .currentPrice(currentPrice)
                .statusMessage(status)
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
