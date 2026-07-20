package com.exchange.engine;

/**
 * A completed match between a buy order and a sell order. Immutable — once a
 * trade happens it never changes (this is what makes the trade log a reliable,
 * replayable record).
 *
 * The price here is the RESTING (maker) order's price, not the incoming order's.
 * Price improvement always goes to the incoming (taker) side.
 */
public final class Trade {

    private final long buyOrderId;
    private final long sellOrderId;
    private final long price;     // in ticks
    private final long quantity;

    public Trade(long buyOrderId, long sellOrderId, long price, long quantity) {
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
    }

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

    @Override
    public String toString() {
        return "Trade{buy=" + buyOrderId + ", sell=" + sellOrderId
                + ", price=" + price + ", qty=" + quantity + '}';
    }
}
