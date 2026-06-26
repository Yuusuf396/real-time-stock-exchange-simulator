package com.exchange.engine;

/**
 * LIMIT  — rests in the book at its price if not fully filled.
 * MARKET — sweeps the book until filled or empty; never rests.
 */
public enum OrderType {
    LIMIT,
    MARKET
}
