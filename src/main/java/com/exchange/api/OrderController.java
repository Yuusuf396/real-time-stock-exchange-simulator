package com.exchange.api;

import com.exchange.api.dto.OrderBookResponse;
import com.exchange.api.dto.OrderRequest;
import com.exchange.api.dto.TradeResponse;
import com.exchange.engine.Order;
import com.exchange.engine.Trade;
import com.exchange.service.ExchangeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HTTP ↔ DTO translation only — no exchange logic belongs here. If an `if`
 * about prices, sides, or quantities appears in this class, it is in the
 * wrong layer.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final ExchangeService exchangeService;

    public OrderController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;

    }

    @PostMapping
    public List<TradeResponse> submitOrder(@RequestBody OrderRequest request) {
        return exchangeService.submitOrder(request);
    }

    @GetMapping("/book")
    public OrderBookResponse getBook() {
        return exchangeService.getBook();
    }

    @GetMapping("/trades")
    public List<TradeResponse> getTrades() {
        return exchangeService.getTrades();
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable long id) {
        return exchangeService.getOrder(id);
    }
}