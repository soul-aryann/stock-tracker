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

import com.project.stock_tracker.dto.PortfolioHoldingRequest;
import com.project.stock_tracker.dto.PortfolioHoldingResponse;
import com.project.stock_tracker.dto.PortfolioSummaryResponse;
import com.project.stock_tracker.entity.PortfolioHolding;
import com.project.stock_tracker.service.PortfolioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    public ResponseEntity<PortfolioHolding> addHolding(@RequestBody PortfolioHoldingRequest request) {
        PortfolioHolding saved = portfolioService.addHolding(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<PortfolioHoldingResponse> getAllHoldings() {
        return portfolioService.getAllHoldings();
    }

    @GetMapping("/summary")
    public PortfolioSummaryResponse getPortfolioSummary() {
        return portfolioService.getPortfolioSummary();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHolding(@PathVariable Long id) {
        portfolioService.deleteHolding(id);
        return ResponseEntity.noContent().build();
    }
}
