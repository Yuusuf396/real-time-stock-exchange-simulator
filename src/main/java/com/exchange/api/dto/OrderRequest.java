package com.exchange.api.dto;

import com.exchange.engine.Side;

public class OrderRequest {
    public Side getSide() {
        return side;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getPrice() {
        return price;
    }

    private Side side;
    private int quantity;
    private long price;

    public void setSide(Side side) {
        this.side = side;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
