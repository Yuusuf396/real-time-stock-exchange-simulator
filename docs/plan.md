# Matching Engine — Execution Plan

## What this is

A correct, in-memory stock exchange **matching engine** in Java — the core that
pairs buyers and sellers. Not a price predictor, not a trading bot. The
deterministic, correctness-critical backend that a real exchange runs on.

**Why this project:** target employers are bank/enterprise-heavy (RBC, TD,
Scotia, BMO, CIBC, IBM, consulting). A matching engine maps directly to bank
capital-markets tech, uses the Java/Spring stack those employers run, and proves
the systems depth that a resume of CRUD apps is missing. The scoped version is
finishable solo in ~4 milestones.

## Scope (locked — do not grow mid-build)

In scope:
- One symbol, in-memory.
- Limit + market orders, price-time priority, partial fills.
- REST API (Spring Boot) — Milestone 3, after the engine is correct.
- Deterministic test suite + a load benchmark.

Out of scope (the things that killed earlier attempts):
- Kafka, Rust, distributed systems.
- Multiple symbols, a fancy frontend.
- Postgres / Redis / persistence — optional, only after the 4 milestones.

## Tech stack

- Java 17 — core engine.
- JUnit 5 — testing.
- Maven — build.
- Spring Boot — Milestone 3 only, and it holds NO matching logic.

## Correctness rules (baked into the scaffold — get these right)

1. **Prices are `long` ticks** (e.g. cents), never `double`. Float rounding
   makes matching non-deterministic. Banks care about this most.
2. **Time priority uses a monotonic `sequence`** the engine stamps on arrival,
   not wall-clock time (two orders can share a millisecond).
3. **Trades print at the resting (maker) price.** Price improvement goes to the
   incoming (taker) side.
4. **A market order never rests.** Unfilled remainder is dropped.
5. **No self-trade prevention** — intentionally out of scope; state it in the README.

---

## Milestone 1 — Core engine (no API, no frameworks)

Implement `OrderBook` then `MatchingEngine.submit()`. The scaffold has the
field structure and a step-by-step algorithm in the Javadoc.

Deliverable: `BUY 100 @ 10`, `SELL 50 @ 9` → `TRADE 50 @ 9`, correct results.

Success criteria:
- Matches correctly; respects price priority; respects time priority (FIFO).
- Handles partial fills; outputs are deterministic.
- *If this is wrong, everything else is worthless.*

## Milestone 2 — Testing + edge cases (where you become good)

Turn the 11 stub tests in `MatchingEngineTest` green, then add more until
behavior is unambiguous. Mindset: break your own engine, don't make it pretty.

Must-pass cases: exact match, partial fill (both sides), no-match, multiple
price levels, FIFO at same price, market sweep, market vs empty book, price
improvement at maker price, limit-no-cross, and the invariant test
(`bookNeverCrossedUnderRandomLoad` — fire 1000+ random orders, assert bestBid <
bestAsk after every one).

Deliverable: 15–25 strong tests, zero ambiguous behavior.

Success criteria: you can honestly say *"this engine is correct under defined rules."*

## Milestone 3 — Spring Boot API (only now)

Expose the engine as a service. Endpoints: `POST /order`, `GET /book`,
`GET /trades`. Spring calls `MatchingEngine.submit()` — the engine stays pure
Java.

Rule: **if matching logic leaks into a controller, the architecture failed.**

Success criteria: clean separation (API vs engine), stateless API layer, engine
still testable on its own.

## Milestone 4 — Polish + real-engineer signal

- Structured trade logging (timestamp, buy id, sell id, price, qty).
- Benchmark: simulate 10k–100k orders, measure throughput + latency (this is
  also the "stock simulation" — synthetic order flow feeding the engine).
- Clean README: architecture diagram, matching rules, test strategy, benchmark
  results.

Resume bullet this produces:
> Built a deterministic order-matching engine in Java/Spring Boot with price-time
> priority (limit + market orders, partial fills); validated via a JUnit suite and
> load simulation at N orders/sec, p99 latency under X ms; deployed on AWS.

## Optional phase (only after 4 milestones are solid)

Postgres persistence, Redis caching, Kafka event stream, web dashboard,
multi-symbol. Jumping here early = failure.

---

 
## Current status

- [x] Maven project + JUnit wired (`mvn test` runs).
- [x] `Side`, `OrderType`, `Order`, `Trade` complete (data/enums).
- [x] `OrderBook`, `MatchingEngine` scaffolded with TODO stubs.
- [x] 11 test stubs as a red→green checklist.
- [x] Milestone 1: `OrderBook` + `MatchingEngine.submit()` implemented; exactMatchFullFill passes (`mvn test` BUILD SUCCESS).
- [ ] Milestone 2: turn the remaining 10 tests green + add edge cases.
- [ ] Milestone 3: Spring Boot API.
- [ ] Milestone 4: logging, benchmark, README.

Start at: `OrderBook.addOrder`. Then ask for a review of the match loop before
writing all the tests.
