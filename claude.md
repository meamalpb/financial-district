# CLAUDE.md

Context file for Claude Code when working in this repository. Read this before making changes.

## What this project is

A virtual investing behavior simulator. Independent virtual investors ("Men") each follow a fixed, frozen strategy, buying/selling a virtual (non-real-money) position in real assets based on real market price data. No real trading occurs — everything is a simulated record.

**Core philosophy — do not violate this:**
- Each Man's strategy is **frozen at creation** and never modified after the fact.
- New behavior = a new Man (new config), never an edit to an existing Man's logic.
- Men are fully independent: no shared capital, no interaction between Men, no Man affects another's data or price.
- Every Man is just a config row read by a shared, generic Strategy Engine — **never** a new service or new codebase per Man.

## Current build scope (MVP)

We are deliberately building the dumbest possible version first to prove the pipeline before adding real strategy logic:

- **Temp Man**: strategy = "always buy" (`AlwaysBuyStrategy`), no dip/average logic at all. Exists purely to validate the end-to-end pipeline (price fetch → decision → ledger update) with numbers simple enough to verify by hand.
- **Man 1** (not yet built): real strategy — dip-triggered dollar-cost-accumulation against a 7-day trailing average. Defer this until Temp Man's pipeline is proven.
- **Man 2** (not yet built, future): identical ruleset to Man 1, but backdated start date, backtested through history then transitions to live. Not in current scope.

Do not build dip logic, selling logic, multi-Man comparison views, or additional assets until explicitly asked — these are intentionally deferred.

## Future direction 

Noted here so future work has the right shape in mind, but nothing below should be built until explicitly asked. The "Current build scope" and "What NOT to do" sections above still govern what to actually implement.

- **Historical backfill**: plan is to pull years of daily OHLC history (a handful of stocks to start, SPY first) via price-service's Twelve Data historical client (`TwelveDataProvider.getDailyHistory`, `time_series` endpoint), stored in the `price_history` Mongo collection (already named in the data flow diagram below) separate from the live `quotes` cache. At daily granularity this is a small dataset (thousands of rows, not a big-data problem). Alpha Vantage was evaluated and rejected: its free tier caps `TIME_SERIES_DAILY` at 100 bars (`outputsize=full` is a paywalled premium feature). Twelve Data's `time_series` endpoint (same key already used for live quotes) caps each call at 5000 bars, so full-history backfill requires pagination via `end_date` (confirmed: SPY's full 1993–present history takes exactly 2 paginated calls, 8442 bars total, well within free-tier rate limits). This backfill is what Man 2's backtesting would eventually read from.
- **Hand-written simulations**: beyond Man 1/Man 2, the intent is to hand-write simulations against this historical data, including real-world factors that eat into profit — taxation being the first example (capital gains treatment, holding-period thresholds, wash sales, etc.). The expected shape: this is a **post-hoc analytics-layer concern**, computed by reading Ledger's existing transaction history (timestamps, cost basis, realized/unrealized amounts) — not logic baked into a Man's frozen strategy or into Ledger's core mutation path. Keeps it consistent with "Men are frozen and independent."

## Architecture

Spring Boot microservices, Docker Compose (no Kubernetes/orchestration needed), Eureka for service discovery, Spring Cloud Config Server for centralized config, MongoDB (managed, e.g. Atlas) for storage. Kafka is the intended transport between Strategy Engine and Ledger, but **is not wired in yet** — currently using direct REST calls as a temporary stand-in (see "Known temporary stand-ins" below).

### Services

| Service | Status | Responsibility |
|---|---|---|
| **discovery-service** | Built | Eureka server |
| **config-server** | Built | Centralized config for all services |
| **price-service** | Built | Only service allowed to call external stock APIs. Currently wraps Twelve Data's quote endpoint, caches results in Mongo (`quotes` collection), serves via `GET /internal/prices/{symbol}`. Twelve Data historical client (`TwelveDataProvider.getDailyHistory`, `time_series` endpoint) and `PriceHistory`/`PriceHistoryRepository` model exist; the backfill orchestration (paginate + idempotently persist to `price_history`) is not yet built. |
| **strategy-service** | In progress | Reads Man config, fetches price from price-service, applies a `Strategy` implementation, produces a `BuyDecision`. Currently exposes `GET /internal/run-temp-man` to manually trigger one cycle for Temp Man. |
| **ledger-service** | In progress | Source of truth for each Man's money: bank balance, transactions, shares owned, cost basis, market value. Only service allowed to mutate this state. |
| **analytics-service** | Not started | Read-only aggregation over Ledger data into graph-ready time series (cost basis vs market value, gain/loss %). |
| **api-gateway** | Not started | Single entry point, routes to Analytics/Ledger. |

### Data flow (target end state)

```
[External APIs: Twelve Data (live quotes + historical time_series)]
        │
        ▼
[price-service] → stores → (quotes / price_history in Mongo)
        │
        ▼
[strategy-service] → reads Man config → decides buy/no-buy
        │
        │  Kafka: BuyDecisionMade event  ← NOT YET WIRED, see below
        ▼
[ledger-service] → idempotently applies → (man_accounts, transactions in Mongo)
        │  (read-only)
        ▼
[analytics-service] → serves → [api-gateway] → [frontend]
```

## Known temporary stand-ins — do not treat these as final design

These exist purely to unblock incremental testing. Each has a flagged reason and an intended replacement. Don't "clean these up" without checking whether the replacement is actually ready.

- **strategy-service currently has no `ManConfigRepository`/Mongo integration.** `StrategyEngineService.getTempManConfig()` hardcodes a single in-memory `ManConfig` for `temp-man-1`. Replace with a real Mongo-backed repository once there's more than one Man to manage.
- **`ManConfig.currentBalance` is a stubbed field on the config object itself.** In the real design, Ledger is the *only* source of truth for balance — Strategy Engine should ask Ledger for the current balance via a REST/Kafka call, not carry its own copy. This field exists only because Ledger didn't exist yet when Strategy Engine was being tested standalone.
- **Strategy Engine → Ledger communication is currently planned as direct REST** (`POST /internal/buy`), not Kafka. Kafka producer/consumer wiring is the next major piece of work, deferred until Ledger's logic is proven correct via manual REST calls first.
- **No idempotency/dedupe logic in Ledger yet.** Needed once Kafka is introduced (to handle redelivered/duplicate events) — not needed yet for manually-triggered single REST calls, but must be added before Kafka wiring is considered done.
- **No daily contribution accumulation logic yet** (the "$1/day added to bank" mechanic). Not needed for Temp Man (whose whole point is "always buy," no accumulation to reason about) — becomes necessary once Man 1's real dip-timing logic is built.
- **No daily mark-to-market job in Ledger yet.** Market value should recalculate daily using the latest price, independent of whether a buy happened that day. Not yet implemented.

## Conventions

- Each service has its own DTOs — **do not share model classes across services** (e.g., strategy-service has its own slim `PriceResponse` DTO rather than importing price-service's `Quote`). This is deliberate: services should only depend on the shape of data they need, not on another service's internal model, so services can evolve independently.
- Services communicate via Eureka service names (e.g. `http://price-service/...`), not hardcoded hostnames/ports. Any `RestTemplate` used for inter-service calls must be annotated `@LoadBalanced`.
- `Strategy` is an interface (`decide(ManConfig, BigDecimal currentPrice) -> BuyDecision`) specifically so new strategies (e.g. Man 1's dip logic) can be added as new implementations without touching the scheduler/engine/Kafka plumbing around them.
- Money fields are `BigDecimal`, never `String` or `double`.
- Config Server clients should set `spring.cloud.config.fail-fast: true` so missing/misconfigured config fails loudly at startup instead of silently booting with nothing.

## What NOT to do without being asked

- Don't add dip/average logic to any strategy yet — Temp Man must stay dumb until the pipeline is proven.
- Don't build Man 2, multi-Man comparison views, or additional assets yet.
- Don't introduce a shared library/module between services to reduce DTO duplication — this is intentional decoupling, not an oversight.
- Don't add Kafka wiring until Ledger's REST-based buy/portfolio logic is confirmed correct by manual testing first.