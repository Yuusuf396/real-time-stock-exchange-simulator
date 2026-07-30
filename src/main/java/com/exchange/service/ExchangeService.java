package com.exchange.service;

import com.exchange.api.dto.OrderBookResponse;
import com.exchange.api.dto.OrderRequest;
import com.exchange.api.dto.TradeResponse;
import com.exchange.engine.MatchingEngine;
import com.exchange.engine.Order;
import com.exchange.engine.Trade;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;



/**
 * Owns the single MatchingEngine instance and everything the engine refuses to
 * know about: validation, order ids, trade timestamps, and the engine↔DTO
 * conversion.
 *
 * All public methods are synchronized — the engine is single-threaded by
 * design, and this one lock is what makes concurrent HTTP requests safe.
 * Timestamps are stamped here rather than in the engine so the engine stays
 * deterministic (same orders in → same trades out, no clock involved).
 */
@Service
public class ExchangeService {
    private final MatchingEngine engine;

    private final List<TradeResponse> tradeHistory = new ArrayList<>();
    private final Map<Long, Order> orders = new HashMap<>();

    private long nextOrderId = 1;


    public ExchangeService(){
        this.engine= new MatchingEngine();
    }



    public  synchronized List<TradeResponse> submitOrder(OrderRequest request){
        if((request.getSide()==null) || (request.getPrice()<=0 )|| (request.getQuantity()<=0)){
            throw new IllegalArgumentException("Invalid order");

        }
        Order order=convert(request);
        orders.put(order.getId(),order);
        List<Trade> trades= engine.submit(order);

        List<TradeResponse> responses = new ArrayList<>();

        for(Trade trade:trades){
            responses.add(new TradeResponse(
                    trade.getBuyOrderId(),
                    trade.getSellOrderId(),
                    trade.getPrice(),
                    trade.getQuantity(),
                    Instant.now()));
        }


        tradeHistory.addAll(responses);

        return responses;


    }
    public synchronized List<TradeResponse> getTrades() {
         return new ArrayList<>(tradeHistory);
    }

    public synchronized OrderBookResponse getBook(){
        List<OrderBookResponse.BookEntry> bidEntries = new ArrayList<>();
        List<OrderBookResponse.BookEntry> askEntries = new ArrayList<>();

        for (Order order: engine.getBook().getBidOrders()){
            bidEntries.add(new OrderBookResponse.BookEntry(
                    order.getId(),
                    order.getPrice(),
                    order.getQuantity()
            ));
        }
        for (Order order: engine.getBook().getAsksOrders()){
            askEntries.add(new OrderBookResponse.BookEntry(
                    order.getId(),
                    order.getPrice(),
                    order.getQuantity()
            ));
        }

        return new OrderBookResponse(bidEntries, askEntries);
    }

    public synchronized Order getOrder(long id){
        Order order = orders.get(id);
        if (order == null) {
            throw new NoSuchElementException("Order not found: " + id);
        }
        return order;
    }
    private Order convert(OrderRequest request) {
        long id=nextOrderId++;
        return Order.limit(
                id,
                request.getSide(),
                request.getPrice(),
                request.getQuantity()
        );
    }



}
