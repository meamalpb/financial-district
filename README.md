# financial-district

Virtual investing behavior simulator. Independent virtual investors ("Men") each follow a fixed, frozen strategy, buying/selling a virtual (non-real-money) position in real assets based on real market price data. No real trading occurs — everything is a simulated record.

See [CLAUDE.md](CLAUDE.md) for full architecture, design philosophy, and current build scope.

## Running locally

Single Spring Boot application, port 8080.

```
./run.sh   # starts the app
./stop.sh  # stops it
```

Logs are written to `logs/financial-district.log`, PID to `.pids/financial-district.pid`.

## Configuration

Secrets (Postgres connection info, Twelve Data API key) are not committed. Copy `.env.example` to `.env` in the repo root and fill in real values:

```
cp .env.example .env
```

| Variable | Description |
|---|---|
| `DATASOURCE_URL` | Postgres JDBC connection string (e.g. Neon) |
| `DATASOURCE_USERNAME` | Postgres username |
| `DATASOURCE_PASSWORD` | Postgres password |
| `TWELVEDATA_API_KEY` | Twelve Data API key |

`run.sh` loads `.env` and exports these into the environment before starting the app, since `src/main/resources/application.yml` references them as `${DATASOURCE_URL}` / `${DATASOURCE_USERNAME}` / `${DATASOURCE_PASSWORD}` / `${TWELVEDATA_API_KEY}` placeholders.

## Endpoints

| Endpoint | Description |
|---|---|
| `GET /internal/prices/{symbol}` | Get (and cache) a live quote |
| `POST /internal/prices/{symbol}/backfill` | Backfill full daily price history for a symbol |
| `GET /internal/prices/{symbol}/history?from=...&to=...` | Read backfilled price history |
| `GET /internal/accounts/{manId}` | Get a Man's account (balance, shares, cost basis, gain) |
| `POST /internal/buy` | Manually apply a buy to a Man's ledger |
| `GET /internal/run-temp-man` | Run one live decision cycle for Temp Man |
| `GET /internal/simulate-temp-man?from=...&to=...` | Replay Temp Man's strategy over backfilled history |
