package com.exchange.api.dto;

import java.time.Instant;

public class TradeResponse {
    public long getBuyOrderId() {
        return buyOrderId;
    }



    public long getSellOrderId() {
        return sellOrderId;
    }


    public long getPrice() {
        return price;
    }


    public long getQuantity() {
        return quantity;
    }



    public Instant getTimestamp() {
        return timestamp;
    }


    private final long buyOrderId;
    private final long sellOrderId;
    private final long price;
    private final long quantity;
    private final Instant timestamp;

    public TradeResponse(long buyOrderId, long sellOrderId, long price, long quantity, Instant timestamp) {
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }
}
