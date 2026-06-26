package com.exchange.engine;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.TreeMap;

/**
 * The order book: stores resting orders in price-time priority and hands back
 * the best order on a side. It does NOT match — that decision lives in
 * MatchingEngine. Keep it that way; if matching logic leaks in here, the
 * boundary is broken.
 *
 * Structure:
 *  - price level -> FIFO queue of orders at that price (Deque: add to back,
 *    take from front, which gives time priority within a level).
 *  - bids sorted so the BEST (highest) price is firstKey()  -> reverse order.
 *  - asks sorted so the BEST (lowest) price is firstKey()   -> natural order.
 */
public final class OrderBook {

    private final TreeMap<Long, Deque<Order>> bids = new TreeMap<>(Collections.reverseOrder());
    private final TreeMap<Long, Deque<Order>> asks = new TreeMap<>();

    /** Rest an order at the back of its price level (creates the level if new). */
    public void addOrder(Order order) {
        TreeMap<Long, Deque<Order>> book = (order.getSide() == Side.BUY) ? bids : asks;
        Deque<Order> level = book.get(order.getPrice());
        if (level == null) {
            level = new ArrayDeque<>();
            book.put(order.getPrice(), level);
        }
        level.addLast(order);
    }

    /** The best (front-of-queue) resting order on the given side, or null if that side is empty. */
    public Order peekBest(Side side) {
        TreeMap<Long, Deque<Order>> book = (side == Side.BUY) ? bids : asks;
        var bestLevel = book.firstEntry();          // best price + its queue, or null
        if (bestLevel == null) {
            return null;
        }
        return bestLevel.getValue().peekFirst();    // front order at the best price
    }

    /** Best bid price, or null if there are no bids. */
    public Long bestBidPrice() {
        return bids.isEmpty() ? null : bids.firstKey();
    }

    /** Best ask price, or null if there are no asks. */
    public Long bestAskPrice() {
        return asks.isEmpty() ? null : asks.firstKey();
    }

    /**
     * Reduce the front order on `side` by fillQty. If it reaches zero, remove it
     * from the queue (and drop the price level if the queue becomes empty).
     */
    public void reduceBest(Side side, long fillQty) {
        TreeMap<Long, Deque<Order>> book = (side == Side.BUY) ? bids : asks;
        var bestLevel = book.firstEntry();
        if (bestLevel == null) {
            return;
        }
        Deque<Order> level = bestLevel.getValue();
        Order front = level.peekFirst();
        if (front == null) {
            return;
        }
        long remaining = front.getQuantity() - fillQty;
        if (remaining > 0) {
            front.setQuantity(remaining);           // partially filled, stays in book
        } else {
            level.pollFirst();                      // fully filled, remove it
            if (level.isEmpty()) {
                book.remove(bestLevel.getKey());    // price level now empty, drop it
            }
        }
    }

    public boolean isEmpty() {
        return bids.isEmpty() && asks.isEmpty();
    }

    /** Total resting orders across both sides — useful in tests. */
    public int size() {
        int n = 0;
        for (Deque<Order> level : bids.values()) {
            n += level.size();
        }
        for (Deque<Order> level : asks.values()) {
            n += level.size();
        }
        return n;
    }
}
