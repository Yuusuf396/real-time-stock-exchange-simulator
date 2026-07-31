# Handoff notes

As of 2026-07-30. Details in `docs/plan.md`.

## Done (Milestones 1–3)

- **Engine** (`com.exchange.engine`) — price-time priority matching, limit +
  market orders, partial fills. Pure Java. 12/12 tests green (`mvn test`).
- **API** — `POST /orders`, `GET /orders/book`, `GET /orders/trades`,
  `GET /orders/{id}`. Validation → 400, unknown id → 404.

Run: `mvn spring-boot:run`, then:

```
curl -X POST localhost:8080/orders -H "Content-Type: application/json" \
     -d '{"side":"BUY","price":100,"quantity":50}'
```

## Rules (do not break)

1. No matching logic outside `MatchingEngine`; engine never imports Spring or DTOs.
2. Engine types stay out of controllers — service converts to DTOs.
3. Every public `ExchangeService` method is `synchronized` (engine is single-threaded).
4. Prices/quantities are `long` ticks — never `double` or `int`.
5. No clocks or randomness in the engine; timestamps come from the service.
6. `mvn test` green after every task; one task per commit.

## To do, in order

1. **`GET /orders/{id}`** — returns raw engine `Order` (leaks `sequence`) and
   shows a stale quantity for fully-filled resting orders (the engine removes
   them from the book without zeroing the field). Give it a DTO with an honest
   remaining/filled story, or delete the endpoint.
2. **Dead code** — remove unused `MatchingEngine.getOrders()` and
   `OrderBook.getOrders()`.
3. **Market orders via API** — add `type` (`LIMIT`/`MARKET`) to `OrderRequest`;
   branch in `ExchangeService.convert()`. Market orders skip the price check.
4. **Benchmark** — plain Java, no Spring: fire 10k–100k random orders at the
   engine, measure orders/sec + p50/p99 latency, warm up the JVM first.
   Numbers go in the README.
5. **Trade logging** — SLF4J in the service: timestamp, ids, price, quantity.
6. **README** — architecture, matching rules, test strategy, benchmark results.
