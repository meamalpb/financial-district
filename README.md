# financial-district

Virtual investing behavior simulator. Independent virtual investors ("Men") each follow a fixed, frozen strategy, buying/selling a virtual (non-real-money) position in real assets based on real market price data. No real trading occurs — everything is a simulated record.

See [CLAUDE.md](CLAUDE.md) for full architecture, design philosophy, and current build scope.

## Services & ports

| Service | Port | Status | Notes |
|---|---|---|---|
| discovery-server | 8761 | Built | Eureka server |
| config-server | 8888 | Built | Spring Cloud Config Server (native profile, serves `config-server/src/main/resources/config/*.yml`) |
| price-service | 8083 | Built | Wraps Twelve Data quote endpoint |
| strategy-service | 8084 | In progress | Reads Man config, applies strategy, produces buy decisions |
| ledger-service | 8085 | In progress | Source of truth for each Man's balance, transactions, shares |
| analytics-service | — | Not started | Read-only aggregation over Ledger data |
| api-gateway | — | Not started | Single entry point, routes to Analytics/Ledger |

Ports are defined in each service's `application.yml`/`application.yaml`, except price-service, strategy-service, and ledger-service, whose ports are centralized in `config-server/src/main/resources/config/*.yml` (served via Spring Cloud Config).

## Configuration

Secrets (Mongo URI, Twelve Data API key) are not committed. Copy `.env.example` to `.env` in the repo root and fill in real values:

```
cp .env.example .env
```

| Variable | Used by | Description |
|---|---|---|
| `MONGODB_URI` | price-service, ledger-service | MongoDB Atlas connection string |
| `TWELVEDATA_API_KEY` | price-service | Twelve Data API key |

`run-all.sh` loads `.env` and exports these into the environment before starting services, since `config-server/src/main/resources/config/*.yml` references them as `${MONGODB_URI}` / `${TWELVEDATA_API_KEY}` placeholders (resolved client-side against each service's own environment).

## Running locally

```
./run-all.sh   # starts services in dependency order: discovery -> config -> (price, ledger, strategy)
./stop-all.sh  # stops everything
```

Logs are written to `logs/<service>.log`, PIDs to `.pids/<service>.pid`.
