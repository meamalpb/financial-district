# CLAUDE.md

Context file for Claude Code when working in this repository. Read this before making changes.

## What this project is

A virtual investing behavior simulator. Independent virtual investors ("Men") each follow a fixed, frozen strategy, buying/selling a virtual (non-real-money) position in real assets based on real market price data. No real trading occurs — everything is a simulated record.

**Core philosophy — do not violate this:**
- Each Man's strategy is **frozen at creation** and never modified after the fact.
- New behavior = a new Man (new config), never an edit to an existing Man's logic.
- Men are fully independent: no shared capital, no interaction between Men, no Man affects another's data or price.
- Every Man is just a config row read by a shared, generic Strategy Engine — **never** a new service, module, or codebase per Man.

## Architecture: monolith

This is a **single Spring Boot application**, not a set of microservices. It previously existed as 5 separate services (Eureka discovery, Config Server, price-service, strategy-service, ledger-service) wired together over REST; that history is preserved in git, but the microservices layout no longer exists in the working tree — everything now lives in one deployable app. There is no service discovery, no config server, and no network hop between the price/strategy/ledger domains — they call each other as plain injected Spring beans in the same JVM.

Postgres (managed, e.g. Neon) is still the datastore, via Spring Data JPA.

### Package layout

Packages are organized by business domain, not by technical layer, under `com.financialdistrict`:

```
com.financialdistrict
├── FinancialDistrictApplication.java
├── price/       — the only code allowed to call external stock APIs (Twelve Data)
│   ├── controllers/ services/ providers/ models/ dto/ mappers/ repositories/ config/
├── ledger/      — source of truth for each Man's money
│   ├── controllers/ services/ models/ dto/ repositories/
└── strategy/    — reads Man config, applies a Strategy, calls ledger directly to record buys
    ├── controllers/ services/ strategies/ models/ events/ dto/
```

Each domain keeps its own DTOs/models where it makes sense to isolate a concern (e.g. `price`'s Twelve Data response shapes shouldn't leak into `strategy` or `ledger`), but **domains are free to call each other's service beans and reuse each other's DTOs directly** (e.g. `StrategyEngineService` calls `PriceService` and `LedgerService` beans directly, and reuses `ledger.dto.BuyRequest`/`AccountResponse` and `price.dto.PriceBarResponse` rather than maintaining parallel copies). The old "don't share model classes" rule only made sense when these were separate deployable services communicating over a network boundary — that boundary no longer exists.

### Data flow

```
[External APIs: Twelve Data (live quotes + historical time_series)]
        │
        ▼
[price package]   → stores → (quotes / price_history in Postgres)
        │
        ▼
[strategy package] → reads Man config → decides buy/no-buy
        │
        ▼ (direct method call, in-process)
[ledger package]  → applies → (man_accounts, transactions in Postgres)
```

`analytics` (read-only aggregation over ledger data) and an API gateway/frontend layer do not exist yet — see "Current build scope" below.

## Current build scope (MVP)

We are deliberately building the dumbest possible version first to prove the pipeline before adding real strategy logic:

- **Temp Man**: strategy = "always buy" (`AlwaysBuyStrategy`), no dip/average logic at all. Exists purely to validate the end-to-end pipeline (price fetch → decision → ledger update) with numbers simple enough to verify by hand. **This pipeline is proven**: `StrategyEngineService` calls `LedgerService.applyBuy(BuyRequest)` directly, which persists a `Transaction` and updates the `ManAccount` (bank balance, shares owned, cost basis, market value). `LedgerService.getAccount()` (exposed via `GET /internal/accounts/{manId}`) also returns a derived unrealized `gain`/`gainPercent`.
- **Man 1** (not yet built): real strategy — dip-triggered dollar-cost-accumulation against a 7-day trailing average. Defer this until Temp Man's pipeline is proven.
- **Man 2** (not yet built, future): identical ruleset to Man 1, but backdated start date, backtested through history then transitions to live. Not in current scope.

Do not build dip logic, selling logic, multi-Man comparison views, or additional assets until explicitly asked — these are intentionally deferred.

## Future direction

Noted here so future work has the right shape in mind, but nothing below should be built until explicitly asked. The "Current build scope" and "What NOT to do" sections above/below still govern what to actually implement.

- **Historical backfill**: `price.services.PriceHistoryService.backfill()` already pulls years of daily OHLC history via `TwelveDataProvider.getDailyHistory` (the `time_series` endpoint), stored in the `price_history` Postgres table, idempotently (`ON CONFLICT (symbol, date) DO NOTHING`), paginated via `end_date` (confirmed: SPY's full 1993–present history takes exactly 2 paginated calls, 8442 bars total, well within free-tier rate limits). Alpha Vantage was evaluated and rejected: its free tier caps `TIME_SERIES_DAILY` at 100 bars (`outputsize=full` is a paywalled premium feature). This backfill is what Man 2's backtesting would eventually read from.
- **Hand-written simulations**: beyond Man 1/Man 2, the intent is to hand-write simulations against this historical data, including real-world factors that eat into profit — taxation being the first example (capital gains treatment, holding-period thresholds, wash sales, etc.). The expected shape: this is a **post-hoc analytics-layer concern**, computed by reading Ledger's existing transaction history (timestamps, cost basis, realized/unrealized amounts) — not logic baked into a Man's frozen strategy or into Ledger's core mutation path. Keeps it consistent with "Men are frozen and independent."
- **analytics package / API gateway**: not started. When built, `gain`/`gainPercent` computation should move out of `LedgerService` and into an analytics component that reads Ledger data read-only (see stand-ins below).

## Known temporary stand-ins — do not treat these as final design

These exist purely to unblock incremental testing. Each has a flagged reason and an intended replacement. Don't "clean these up" without checking whether the replacement is actually ready.

- **`strategy` package currently has no `ManConfigRepository`/Postgres-backed config.** `StrategyEngineService.getTempManConfig()` hardcodes a single in-memory `ManConfig` for `temp-man-1`. Replace with a real repository once there's more than one Man to manage.
- **`ManConfig.currentBalance` is a stubbed field on the config object itself.** In the real design, Ledger is the *only* source of truth for balance — Strategy Engine should ask `LedgerService` for the current balance directly (this is now a trivial in-process call, unlike when this stub was written for a separate strategy-service that couldn't yet reach Ledger). Not fixed as part of the monolith conversion since it's a behavior change, not a structural one — flagging it here for whoever picks up Man 1's real balance/contribution logic.
- **No idempotency/dedupe logic in Ledger.** This was originally flagged as needed "once Kafka is introduced" — with the monolith, there is no async event transport at all (direct method calls are simply synchronous, at-most-once), so this becomes moot unless an async/event-driven internal transport is deliberately reintroduced later.
- **No daily contribution accumulation logic yet** (the "$1/day added to bank" mechanic). Not needed for Temp Man (whose whole point is "always buy," no accumulation to reason about) — becomes necessary once Man 1's real dip-timing logic is built.
- **No daily mark-to-market job in Ledger yet.** Market value should recalculate daily using the latest price, independent of whether a buy happened that day. Not yet implemented. As a consequence, `gain`/`gainPercent` (below) only reflects market value as of the last buy, not live price.
- **`gain`/`gainPercent` are computed in `LedgerService.toAccountResponse()` at read time, not persisted.** This is a stopgap: per the target architecture, gain/loss reporting belongs to a read-only analytics component, not to Ledger itself. It lives in Ledger for now only because that component doesn't exist yet.

## Conventions

- Packages are organized by business domain (`price`, `strategy`, `ledger`), each with its own `controllers`/`services`/`models`/`dto`/`repositories` subpackages.
- `Strategy` is an interface (`decide(ManConfig, BigDecimal currentPrice) -> BuyDecision`) specifically so new strategies (e.g. Man 1's dip logic) can be added as new implementations without touching the engine plumbing around them.
- Money fields are `BigDecimal`, never `String` or `double`.
- The `price` package's `RestTemplate` bean is for calling the *external* Twelve Data API only — do not repurpose it for calls between packages; those are plain method calls on injected Spring beans.

## What NOT to do without being asked

- Don't add dip/average logic to any strategy yet — Temp Man must stay dumb until the pipeline is proven.
- Don't build Man 2, multi-Man comparison views, or additional assets yet.
- Don't reintroduce microservices, Eureka, a Config Server, or an async transport (Kafka or otherwise) without being explicitly asked — the monolith is the deliberate current architecture, not an intermediate step.
- Don't split domains into separate deployable modules to "future-proof" this — a new Man is a new config row, not a new service.
