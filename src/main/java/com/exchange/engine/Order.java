package com.exchange.engine;

/**
 * A single order. This is a plain data holder — no matching logic lives here.
 *
 * Two correctness decisions are baked in on purpose:
 *  - price is a long in integer ticks (e.g. cents): 15025 means $150.25.
 *    NEVER use double/float for money — rounding makes matching non-deterministic.
 *  - sequence is a monotonic number the engine stamps on arrival. It breaks ties
 *    between two orders at the same price (time priority). Do not use wall-clock
 *    time for this — two orders can share a millisecond.
 */
public final class Order {

    private final long id;
    private final Side side;
    private final OrderType type;
    private final long price;   // in ticks; ignored for MARKET orders
    private long quantity;      // remaining quantity; shrinks as the order fills
    private long sequence;      // assigned by the engine on submit; 0 until then

    public Order(long id, Side side, OrderType type, long price, long quantity) {
        this.id = id;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
    }

    /** Convenience builder for a limit order. */
    public static Order limit(long id, Side side, long price, long quantity) {
        return new Order(id, side, OrderType.LIMIT, price, quantity);
    }

    /** Convenience builder for a market order (price is irrelevant). */
    public static Order market(long id, Side side, long quantity) {
        return new Order(id, side, OrderType.MARKET, 0L, quantity);
    }

    public long getId() {
        return id;
    }

    public Side getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public long getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }



    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return "Order{id=" + id + ", " + side + ", " + type
                + ", price=" + price + ", qty=" + quantity + ", seq=" + sequence + '}';
    }
}
