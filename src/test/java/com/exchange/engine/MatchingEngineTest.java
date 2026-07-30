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


    @Test
    void partialFillSellSide() {
        // Mirror of the model above, other direction.
        // Arrange: resting BUY 50 @ 100.   Act: SELL 20 @ 100.

        engine.submit(Order.limit(1, Side.SELL, 100, 20));

        // Act: BUY 10 @ 100 — exact opposite.
        List<Trade> trades = engine.submit(Order.limit(2, Side.BUY, 100, 50));

        assertEquals(1, trades.size());
        assertEquals(20, trades.get(0).getQuantity());
        assertEquals(100, trades.get(0).getPrice());
        // ...and the SELL's remaining 30 still rests as the best ask,
        // while the BUY is fully filled (no bids in the book).
        assertEquals(1, engine.getBook().size());
        assertEquals(100, engine.getBook().bestBidPrice());
        assertEquals(30, engine.getBook().peekBest(Side.BUY).getQuantity());
        assertNull(engine.getBook().bestAskPrice());
    }

    @Test
    void noMatchRestsInBook() {
        // Act: submit a single BUY 10 @ 100 into an EMPTY book.
        List<Trade> trades = engine.submit(Order.limit(1, Side.BUY, 100, 10));

        // Assert: trades list is empty; book size is 1; bestBidPrice() is 100.
        assertTrue(trades.isEmpty());
        assertEquals(1, engine.getBook().size());
        assertEquals(100, engine.getBook().bestBidPrice());
    }

    @Test
    void multiplePriceLevels() {
        // Arrange: SELL 10 @ 100 (order 1), SELL 10 @ 101 (order 2).
        engine.submit(Order.limit(1, Side.SELL, 100, 10));
        engine.submit(Order.limit(2, Side.SELL, 101, 10));

        // Act: BUY 15 @ 101 — big enough to clear level 100 and dip into 101.
        List<Trade> trades = engine.submit(Order.limit(3, Side.BUY, 101, 15));
        // Assert: TWO trades, in order: 10 @ 100 first, then 5 @ 101.
        assertEquals(2, trades.size());
        assertEquals(100, trades.get(0).getPrice());
        assertEquals(10, trades.get(0).getQuantity());
        assertEquals(1, trades.get(0).getSellOrderId());

        assertEquals(101, trades.get(1).getPrice());
        assertEquals(5, trades.get(1).getQuantity());
        assertEquals(2, trades.get(1).getSellOrderId());

        assertEquals(1, engine.getBook().size());
        assertEquals(101, engine.getBook().bestAskPrice());
        assertEquals(5, engine.getBook().peekBest(Side.SELL).getQuantity());
        assertNull(engine.getBook().bestBidPrice());


        //         Book has one order left: the SELL @ 101 with 5 remaining.
        // (This proves the engine walks levels best-price-first.)
    }

    @Test
    void fifoTimePriorityAtSamePrice() {
        // Arrange: SELL 10 @ 100 (order 1), then SELL 10 @ 100 (order 2) — SAME price.
        engine.submit(Order.limit(1, Side.SELL, 100, 10));
        engine.submit(Order.limit(2, Side.SELL, 100, 10));

        // Act: BUY 10 @ 100.
        List<Trade> trades = engine.submit(Order.limit(3, Side.BUY, 100, 10));
        // Assert: one trade whose sellOrderId is 1 (the EARLIER order), not 2.

        assertEquals(1, trades.size());
        assertEquals(1, trades.get(0).getSellOrderId());   // order 1, not 2
        assertEquals(10, trades.get(0).getQuantity());
        assertEquals(100, trades.get(0).getPrice());

        assertEquals(1, engine.getBook().size());
        assertEquals(10, engine.getBook().peekBest(Side.SELL).getQuantity());
        assertEquals(2, engine.getBook().peekBest(Side.SELL).getId());

        //         Order 2 still rests with its full 10.
        // (This proves the Deque's FIFO gives time priority.)
    }

    @Test
    void takerGetsPriceImprovementAtMakerPrice() {
        // Arrange: resting SELL 10 @ 100.
        engine.submit(Order.limit(1, Side.SELL, 100, 10));
        // Act: BUY 10 @ 105 — willing to pay MORE than the ask.
        List<Trade> trades = engine.submit(Order.limit(2, Side.BUY, 105, 10));

        // Assert: the trade prints at 100 (the maker/resting price), NOT 105.
        assertEquals(1, trades.size());
        assertEquals(100, trades.get(0).getPrice());
        assertEquals(10, trades.get(0).getQuantity());
        assertEquals(1, trades.get(0).getSellOrderId());
        assertEquals(2, trades.get(0).getBuyOrderId());
        assertTrue(engine.getBook().isEmpty());
        // (Correctness rule #3. The equal-price tests can't catch this bug.)
    }

    @Test
    void limitDoesNotCrossWhenPriceWorse() {
        // Arrange: resting SELL 10 @ 101.
        engine.submit(Order.limit(1, Side.SELL, 101, 10));

        // Act: BUY 10 @ 100 — not willing to pay the ask.
        List<Trade> trades = engine.submit(Order.limit(2, Side.BUY, 100, 10));
        // Assert: NO trades; book size is 2 (both rest);
        assertEquals(0, trades.size());
        assertEquals(2, engine.getBook().size());
        assertEquals(100, engine.getBook().bestBidPrice());
        assertEquals(101, engine.getBook().bestAskPrice());


        //         bestBidPrice() is 100 and bestAskPrice() is 101
    }

    @Test
    void marketOrderSweepsBook() {
        // Arrange: SELL 10 @ 100, SELL 10 @ 105 — note the price gap.
        engine.submit(Order.limit(1, Side.SELL, 100, 10));
        engine.submit(Order.limit(2, Side.SELL, 105, 10));
        // Act: MARKET BUY 15 (Order.market(...) — no price).
        List<Trade> trades = engine.submit(Order.market(3, Side.BUY, 15));
        // Assert: two trades — 10 @ 100 then 5 @ 105 (a market order ignores
        assertEquals(2, trades.size());
        assertEquals(10, trades.get(0).getQuantity());
        assertEquals(100, trades.get(0).getPrice());
        assertEquals(5, trades.get(1).getQuantity());
        assertEquals(105, trades.get(1).getPrice());

        assertEquals(1, engine.getBook().size());
        assertEquals(105, engine.getBook().bestAskPrice());
        assertEquals(5, engine.getBook().peekBest(Side.SELL).getQuantity());


        //         price and keeps sweeping). SELL @ 105 has 5 left resting.
    }

    @Test
    void marketOrderEmptyBookDropsRemainder() {
        // Act: MARKET BUY 10 into an EMPTY book.
        List<Trade> trades = engine.submit(Order.market(1, Side.BUY, 10));
        // Assert: no trades AND the book is still empty — the market order
        assertEquals(0, trades.size());
        assertTrue(engine.getBook().isEmpty());
        //         did NOT rest (correctness rule #5).
    }

    @Test
    void marketOrderPartialFillDropsRemainder() {
        // Arrange: resting SELL 4 @ 100 only.
        engine.submit(Order.limit(1, Side.SELL, 100, 4));

        List<Trade> trades = engine.submit(Order.market(2, Side.BUY, 10));
        assertEquals(1, trades.size());
        assertEquals(4, trades.get(0).getQuantity());
        assertEquals(100, trades.get(0).getPrice());
        assertTrue(engine.getBook().isEmpty());


        // Assert: one trade of 4 @ 100; book is EMPTY — the market order's
        //         unfilled 6 vanished instead of resting.
    }

    @Test
    void bookNeverCrossedUnderRandomLoad() {

        Random rng = new Random(42);
        for (int i = 1; i <= 2000; i++) {
            Side side = rng.nextBoolean() ? Side.SELL : Side.BUY;
            long price = 90 + rng.nextInt(21);
            long quantity = 1 + rng.nextInt(50);
            engine.submit(Order.limit(i, side, price, quantity));


            Long bid = engine.getBook().bestBidPrice();
            Long ask = engine.getBook().bestAskPrice();
            if (bid != null && ask != null) {
                assertTrue(bid < ask, "book crossed at order " + i);


            }

        }

    }


}