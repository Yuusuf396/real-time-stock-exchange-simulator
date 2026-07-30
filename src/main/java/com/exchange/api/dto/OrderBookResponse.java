package com.exchange.api.dto;

import java.util.List;

/**
 * Public shape of the order book: bids and asks as separate best-first lists.
 * DTOs like this are the only classes Jackson may serialize — engine types
 * (Order, Trade) must never cross the API boundary directly.
 */
public class OrderBookResponse {

    private final List<BookEntry> bids;
    private final List<BookEntry> asks;

    public OrderBookResponse(List<BookEntry> bids, List<BookEntry> asks) {
        this.bids = bids;
        this.asks = asks;
    }

    public List<BookEntry> getBids() {
        return bids;
    }

    public List<BookEntry> getAsks() {
        return asks;
    }

    public static class BookEntry {
        private final long id;
        private final long price;
        private final long quantity;

        public BookEntry(long id, long price, long quantity) {
            this.id = id;
            this.price = price;
            this.quantity = quantity;
        }

        public long getId() {
            return id;
        }

        public long getPrice() {
            return price;
        }

        public long getQuantity() {
            return quantity;
        }
    }
}
