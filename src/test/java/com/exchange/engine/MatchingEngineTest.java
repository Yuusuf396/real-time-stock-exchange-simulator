package com.exchange.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * WEEK 2 — the correctness suite. Work top to bottom, turning each red test
 * green. When they all pass you can honestly say: "this engine is correct
 * under defined rules."
 *
 * Every test follows the same rhythm:
 *   Arrange — put the book in a known state (submit some resting orders)
 *   Act     — submit the one order the test is about
 *   Assert  — check the trades returned AND the state of the book after
 *
 * The first TWO tests are fully worked as models. The rest are yours:
 * each stub tells you the scenario and exactly what to assert — you write
 * the code. Predict the expected numbers BEFORE running; that prediction
 * is where the learning happens.
 */
class MatchingEngineTest {

    private MatchingEngine engine;

    @BeforeEach
    void setUp() {
        // JUnit runs this before every @Test — each test gets a fresh engine.
        engine = new MatchingEngine();
    }

    // ── MODEL TESTS (done — study the pattern) ──────────────────────────────

    @Test
    void exactMatchFullFill() {
        // Arrange: one resting SELL 10 @ 100.
        engine.submit(Order.limit(1, Side.SELL, 100, 10));

        // Act: BUY 10 @ 100 — exact opposite.
        List<Trade> trades = engine.submit(Order.limit(2, Side.BUY, 100, 10));

        // Assert: one trade, 10 @ 100, right ids, book empty.
        assertEquals(1, trades.size());
        assertEquals(100, trades.get(0).getPrice());
        assertEquals(10, trades.get(0).getQuantity());
        assertEquals(2, trades.get(0).getBuyOrderId());
        assertEquals(1, trades.get(0).getSellOrderId());
        assertTrue(engine.getBook().isEmpty());
    }

    @Test
    void partialFillBuySide() {
        // Arrange: big resting SELL 50 @ 100.
        engine.submit(Order.limit(1, Side.SELL, 100, 50));

        // Act: smaller BUY 20 @ 100.
        List<Trade> trades = engine.submit(Order.limit(2, Side.BUY, 100, 20));

        // Assert: one trade of 20 @ 100...
        assertEquals(1, trades.size());
        assertEquals(20, trades.get(0).getQuantity());
        assertEquals(100, trades.get(0).getPrice());
        // ...and the SELL's remaining 30 still rests as the best ask,
        // while the BUY is fully filled (no bids in the book).
        assertEquals(1, engine.getBook().size());
        assertEquals(100, engine.getBook().bestAskPrice());
        assertEquals(30, engine.getBook().peekBest(Side.SELL).getQuantity());
        assertNull(engine.getBook().bestBidPrice());
    }

    // ── YOUR TESTS (Week 2 checklist — replace each fail() with real code) ──

    @Test
    void partialFillSellSide() {
        // Mirror of the model above, other direction.
        // Arrange: resting BUY 50 @ 100.   Act: SELL 20 @ 100.
        // Assert: 1 trade of 20 @ 100; book has ONE order left — the BUY with
        //         30 remaining as bestBidPrice(); no asks (bestAskPrice() null).
        fail("TODO");
    }

    @Test
    void noMatchRestsInBook() {
        // Act: submit a single BUY 10 @ 100 into an EMPTY book.
        // Assert: trades list is empty; book size is 1; bestBidPrice() is 100.
        fail("TODO");
    }

    @Test
    void multiplePriceLevels() {
        // Arrange: SELL 10 @ 100 (order 1), SELL 10 @ 101 (order 2).
        // Act: BUY 15 @ 101 — big enough to clear level 100 and dip into 101.
        // Assert: TWO trades, in order: 10 @ 100 first, then 5 @ 101.
        //         Book has one order left: the SELL @ 101 with 5 remaining.
        // (This proves the engine walks levels best-price-first.)
        fail("TODO");
    }

    @Test
    void fifoTimePriorityAtSamePrice() {
        // Arrange: SELL 10 @ 100 (order 1), then SELL 10 @ 100 (order 2) — SAME price.
        // Act: BUY 10 @ 100.
        // Assert: one trade whose sellOrderId is 1 (the EARLIER order), not 2.
        //         Order 2 still rests with its full 10.
        // (This proves the Deque's FIFO gives time priority.)
        fail("TODO");
    }

    @Test
    void takerGetsPriceImprovementAtMakerPrice() {
        // Arrange: resting SELL 10 @ 100.
        // Act: BUY 10 @ 105 — willing to pay MORE than the ask.
        // Assert: the trade prints at 100 (the maker/resting price), NOT 105.
        // (Correctness rule #3. The equal-price tests can't catch this bug.)
        fail("TODO");
    }

    @Test
    void limitDoesNotCrossWhenPriceWorse() {
        // Arrange: resting SELL 10 @ 101.
        // Act: BUY 10 @ 100 — not willing to pay the ask.
        // Assert: NO trades; book size is 2 (both rest);
        //         bestBidPrice() is 100 and bestAskPrice() is 101.
        fail("TODO");
    }

    @Test
    void marketOrderSweepsBook() {
        // Arrange: SELL 10 @ 100, SELL 10 @ 105 — note the price gap.
        // Act: MARKET BUY 15 (Order.market(...) — no price).
        // Assert: two trades — 10 @ 100 then 5 @ 105 (a market order ignores
        //         price and keeps sweeping). SELL @ 105 has 5 left resting.
        fail("TODO");
    }

    @Test
    void marketOrderEmptyBookDropsRemainder() {
        // Act: MARKET BUY 10 into an EMPTY book.
        // Assert: no trades AND the book is still empty — the market order
        //         did NOT rest (correctness rule #5).
        fail("TODO");
    }

    @Test
    void marketOrderPartialFillDropsRemainder() {
        // Arrange: resting SELL 4 @ 100 only.
        // Act: MARKET BUY 10.
        // Assert: one trade of 4 @ 100; book is EMPTY — the market order's
        //         unfilled 6 vanished instead of resting.
        fail("TODO");
    }

    @Test
    void bookNeverCrossedUnderRandomLoad() {
        // THE INVARIANT TEST — catches more bugs than all the others combined.
        //
        // Plan:
        //   Random rng = new Random(42);        // fixed seed -> deterministic
        //   loop i = 1..2000:
        //     side  = rng.nextBoolean() ? BUY : SELL
        //     price = 90 + rng.nextInt(21)      // 90..110
        //     qty   = 1 + rng.nextInt(50)       // 1..50
        //     engine.submit(Order.limit(i, side, price, qty));
        //     THEN assert the invariant: if both bestBidPrice() and
        //     bestAskPrice() are non-null, bid must be STRICTLY LESS than ask.
        //     (If they ever overlap, the engine failed to match them - a bug.)
        //
        // Hint: message form of assertTrue helps debugging:
        //   assertTrue(bid < ask, "book crossed at order " + i);
        fail("TODO");
    }
}
