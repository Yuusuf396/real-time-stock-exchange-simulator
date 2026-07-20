package com.exchange.engine;

/** Which side of the book an order sits on. */
public enum Side {
    BUY,
    SELL;

    /** The opposite side — handy when walking the book to find matches. */
    public Side opposite() {
        return this == BUY ? SELL : BUY;
    }
}
