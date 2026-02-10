# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Local-first Yahoo Fantasy Basketball helper for data ingestion, normalization, and analysis. Spring Boot 4 / Java 21 / PostgreSQL. Emphasizes correctness, extensibility, and production-grade patterns (Flyway migrations, sync state, testing).

## Commands

- `docker compose up -d db` — Start PostgreSQL
- `./gradlew bootRun` — Run app (HTTPS at `https://localhost:8443`)
- `./gradlew test` — Run all tests
- `./gradlew test --tests "com.example.fbyahoo.SomeTest"` — Run a single test class
- `./gradlew test --tests "com.example.fbyahoo.SomeTest.methodName"` — Run a single test method
- `docker compose down -v && docker compose up -d db` — Nuke and recreate database

## Critical Invariants

- **Manual OAuth:** Custom Yahoo OAuth 2.0 flow (not Spring Security OAuth2 auto-config). CSRF is disabled — don't re-enable without updating OAuth endpoints. State parameter stored in `HttpSession["state"]`.
- **HTTPS Only:** `https://localhost:8443` — Yahoo requires HTTPS redirect URIs. Uses mkcert + PKCS12 keystore in `secrets/keystore.p12`.
- **Database:** Flyway-managed schema only (`src/main/resources/db/migration/`). Hibernate `ddl-auto=validate` — never let Hibernate create or alter tables.
- **Token Model:** Single-row `oauth_token` table. `TokenService.findTopByOrderByIdAsc()` — `saveToken()` calls `deleteAll()` first to maintain this invariant.
- **Secrets:** Loaded from `.env` via `spring.config.import=optional:file:.env[.properties]`. Never commit `.env`, keystores, or certificates.

## Architecture

**Package:** `com.example.fbyahoo`

```
config/          — Spring beans: SecurityConfig, HttpClientConfig (WebClient beans), YahooProperties
controller/      — YahooOAuthController (/oauth/yahoo/**), HomeController, OAuthExceptionHandler
controller/ingestion/ — Ingestion trigger endpoints (GET /ingest/{entity}/all → redirect:/success)
service/         — TokenService (token lifecycle + auto-refresh), YahooOAuthService (authorize URL, token exchange)
service/ingestion/   — Per-entity ingestion services that call Yahoo Fantasy API and upsert to DB
model/           — JPA entities (League, Team, Player, PlayerOwnership, PlayerStats, LeagueRosteredPlayer)
repo/            — Spring Data JPA repositories
dto/             — YahooTokenResponse record
enums/           — OAuthFailureReason
exception/       — OAuthFlowException
util/            — YahooJson (null-safe JSON field extractors for Yahoo's unusual response format)
```

### Key Patterns

**Two WebClient beans** (defined in `HttpClientConfig`):
- `yahooOauthClient` — base URL `https://api.login.yahoo.com` for token exchange/refresh
- `yahooFantasyClient` — base URL `https://fantasysports.yahooapis.com` for Fantasy API calls

**Yahoo API config** is bound via `YahooProperties` (`@ConfigurationProperties(prefix = "yahoo")`) with nested `oauth` and `api` groups.

**Ingestion pattern:** Each entity has a controller (`GET /ingest/{entity}/all`) that delegates to an `*IngestionService`. The service calls the Yahoo Fantasy API via `yahooFantasyClient`, parses the deeply nested JSON using `YahooJson` utility helpers, then upserts via JPA. All ingestion methods are `@Transactional`.

**Yahoo JSON parsing:** Yahoo's Fantasy API returns a non-standard JSON structure where arrays contain objects with single keys. The `YahooJson` utility (`text()`, `intOrNull()`, `boolOrNull()`, `dateFieldOrNull()`) handles null-safe extraction. Player data requires walking an array of singleton objects to collect fields. See `docs/research/league-ingestion/` for response structure documentation.

**Token auto-refresh:** `TokenService.getValidAccessToken()` checks expiry (with 30s buffer), refreshes via `YahooOAuthService.refreshToken()` if needed, and clears tokens on refresh failure (forcing re-login).

### Database Schema (Flyway)

Migrations in `src/main/resources/db/migration/`:
- `V1__token_table.sql` — `oauth_token` (single-row)
- `V2__league_team_tables.sql` — `league`, `team`, `player`, `player_ownership`, `league_rostered_player`, `player_stats`

Entity keys follow Yahoo's key format: `466.l.42086` (league), `466.l.42086.t.3` (team), `466.p.5009` (player). Game key `466` = NBA 2024-25 season.

### Ingestion Endpoints

All triggered via GET (browser-friendly), redirect to `/success`:
- `/ingest/league/all` — leagues for current user
- `/ingest/team/all?leagueKey=...` — teams in a league
- `/ingest/player/all` — all players for game key 466 (paginated, 25/page)
- `/ingest/lrp/all?leagueKey=...` — league rostered players

## Logging & Errors

- Use `log.info`/`log.warn` with event names. Never log raw tokens or `.env` values.
- OAuth failures flow through `OAuthFlowException` with `OAuthFailureReason` enum, handled by `OAuthExceptionHandler`.

## Rules
- Always follow formatting and naming conventions in existing code.
- When implementing new features, follow the patterns of the previous related features (i.e. implementing new service, refer to other services)