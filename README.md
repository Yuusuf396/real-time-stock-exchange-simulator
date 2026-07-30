# Real-Time Stock Exchange — Matching Engine

A correct, in-memory **order-matching engine** in Java — the core that powers a
stock exchange. Incoming buy/sell orders are paired into trades using
**price-time priority**; unmatched orders rest in the order book until they can
trade. This is the deterministic, correctness-critical backend that an exchange
runs on — not a price predictor or a trading bot.

> **Status:** Milestones 1–3 complete — engine implemented and verified (12/12
> tests green), REST API live with validation, error handling (400/404), and a
> DTO layer. Milestone 4 (benchmark + logging + docs) in progress.

---

## What it does

Send the engine orders; it pairs the ones that can trade and keeps the rest
waiting. A worked example:

    SELL #1  10 @ $100      → rests (no buyers yet)
    SELL #2   5 @ $101      → rests
    BUY  #3  12 @ $100      → matches #1 for 10 @ $100 (a TRADE)
                              → can't reach #2 ($101 > $100), so 2 left over rests

    Book after:   ASKS 101 → [#2: 5]
                  BIDS 100 → [#3: 2]
    Trade:        #3 bought from #1 — 10 @ $100

---

## Matching rules

1. **Price-time priority** — best price matches first; ties broken by arrival order (FIFO).
2. **Integer prices** — money is a `long` in ticks (e.g. cents), never `double`. Floats make matching non-deterministic.
3. **Trades print at the maker (resting) price** — price improvement goes to the incoming (taker) side.
4. **Partial fills** — an order can fill across several resting orders; any remainder of a limit order rests.
5. **Market orders never rest** — they sweep the book until filled, then drop any remainder.

> No self-trade prevention — intentionally out of scope for this version.

---

## Architecture

```
MatchingEngine.submit(order)     ← the brain: runs the match loop
        │ calls
        ▼
OrderBook (addOrder / peekBest / reduceBest)   ← stores resting orders
        │ built from
        ▼
Order · Trade · Side · OrderType               ← data + enums
```

| Class | Responsibility |
|-------|----------------|
| `Order` | one buy/sell request (id, side, type, price, qty, sequence) |
| `OrderBook` | stores resting orders in price-time priority; returns the best on a side |
| `MatchingEngine` | the only place matching decisions are made — the match loop |
| `Trade` | an immutable record of one completed match |

### Data structure

The book is two `TreeMap<Long, Deque<Order>>` — one per side:

- **`TreeMap` (sorted by price)** gives **price priority** — the best price is always `firstKey()`. Bids sort highest-first, asks lowest-first.
- **`Deque` (FIFO queue) per price level** gives **time priority** — new orders join the back, matches take from the front.

This is the minimum structure that makes both priority rules fast at once.

---

## Build & run

Requires Java 17+ and Maven.

```bash
mvn test          # run the test suite
mvn test-compile  # compile only
```

---

## Roadmap

- [x] Milestone 1 — core engine (price-time priority, partial fills, market orders)
- [x] Milestone 2 — full correctness test suite + edge cases (12 tests, incl. random-load invariant)
- [x] Milestone 3 — Spring Boot REST API (`POST /orders`, `GET /orders/book`, `GET /orders/trades`)
- [ ] Milestone 4 — benchmark (throughput + latency) + structured trade logging

---

## Tech stack

Java 17 · JUnit 5 · Maven · Spring Boot
