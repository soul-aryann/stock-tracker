package com.project.stock_tracker.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.stock_tracker.dto.PriceAlertRequest;
import com.project.stock_tracker.dto.PriceAlertResponse;
import com.project.stock_tracker.entity.PriceAlert;
import com.project.stock_tracker.service.PriceAlertService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class PriceAlertController {

    private final PriceAlertService priceAlertService;

    @PostMapping
    public ResponseEntity<PriceAlert> createAlert(@RequestBody PriceAlertRequest request) {
        PriceAlert alert = priceAlertService.createAlert(request);
        return ResponseEntity.ok(alert);
    }

    @GetMapping
    public List<PriceAlertResponse> getAllAlerts() {
        return priceAlertService.getAllAlerts();
    }

    @PostMapping("/check")
    public List<PriceAlertResponse> checkAlerts() {
        return priceAlertService.checkAndTriggerAlerts();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        priceAlertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }
}
