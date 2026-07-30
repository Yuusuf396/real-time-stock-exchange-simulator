package com.exchange.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * The matching engine — the ONLY place matching decisions are made.
 *
 * This is the heart of the project. Everything else (Spring, AWS, the
 * benchmark) bolts onto a correct engine.
 */
public final class MatchingEngine {

    private final OrderBook book = new OrderBook();
    private long sequenceCounter = 0;

    /**
     * Submit an order. Returns every trade it generated, in fill order.
     * See the match loop below — that loop IS the exchange.
     */
    public List<Trade> submit(Order order) {
        List<Trade> trades = new ArrayList<>();
        order.setSequence(++sequenceCounter);          // 1. stamp arrival order (time priority)
        Side opposite = order.getSide().opposite();

        while (order.getQuantity() > 0) {              // 2. keep matching while we still want shares
            Order resting = book.peekBest(opposite);   //    look at the best opposite order
            if (resting == null) {
                break;                                 //    nothing on the other side -> stop
            }
            if (order.getType() == OrderType.LIMIT && !crosses(order, resting)) {
                break;                                 //    prices don't overlap -> stop
            }

            long fill = Math.min(order.getQuantity(), resting.getQuantity());
            long tradePrice = resting.getPrice();      //    trade prints at the MAKER (resting) price

            long buyId  = (order.getSide() == Side.BUY)  ? order.getId() : resting.getId();
            long sellId = (order.getSide() == Side.SELL) ? order.getId() : resting.getId();
            trades.add(new Trade(buyId, sellId, tradePrice, fill));

            book.reduceBest(opposite, fill);           //    shrink/remove the resting order
            order.setQuantity(order.getQuantity() - fill);  // shrink the incoming order
        }

        // 3. leftover: a LIMIT order rests; a MARKET order's remainder is dropped.
        if (order.getQuantity() > 0 && order.getType() == OrderType.LIMIT) {
            book.addOrder(order);
        }
        return trades;
    }

    /** Do the incoming order's price and the resting order's price overlap? */
    private boolean crosses(Order incoming, Order resting) {
        if (incoming.getSide() == Side.BUY) {
            return incoming.getPrice() >= resting.getPrice();  // willing to pay at least the ask
        } else {
            return incoming.getPrice() <= resting.getPrice();  // willing to accept at most the bid
        }
    }

    /** Read-only access for tests and (later) the API layer. */
    public OrderBook getBook() {
        return book;
    }
    public List<Order> getOrders(){
        return book.getOrders();
    }
}
