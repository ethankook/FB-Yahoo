# Fantasy Basketball Helper 🏀

A local-first Yahoo Fantasy Basketball analytics tool built with Spring Boot and React.

## Features

- 📊 **League Overview** - View all your Yahoo Fantasy leagues
- 👥 **Team Dashboard** - Detailed stats for your team
- 📈 **Standings** - Real-time league standings with W-L-T records
- 🤝 **Matchup Analysis** - Week-by-week category matchup tracking
- 🎯 **Player Recommendations** - Top available players by category (PTS, REB, AST, STL, BLK, etc.)
- 💡 **Team Insights** - Identify your strongest and weakest categories
- 🔄 **Data Sync** - One-click sync with Yahoo Fantasy API

## Tech Stack

**Backend:**
- Java 21
- Spring Boot 4
- PostgreSQL 16
- Flyway migrations
- Yahoo OAuth 2.0

**Frontend:**
- React 18
- TypeScript
- Vite
- React Router

## Quick Start with Docker 🐳

The easiest way to run the entire stack:

```bash
# 1. Copy environment template
cp .env.example .env

# 2. Edit .env with your Yahoo credentials
# Get credentials from: https://developer.yahoo.com/apps/

# 3. Ensure SSL certificates are in place
mkdir -p secrets
cp src/main/resources/keystore.p12 secrets/

# 4. Start everything
./start.sh

# Or manually:
docker compose up --build
```

Open `https://localhost:8443` and login with Yahoo!

See [DOCKER.md](DOCKER.md) for detailed Docker documentation.

## Development Setup

For active development with hot reload:

### Prerequisites

- Java 21
- Node.js 22+
- Docker Desktop
- mkcert (for local HTTPS)

### 1. Database

```bash
docker compose up -d db
```

### 2. Backend

```bash
# Create .env file with credentials
cp .env.example .env
# Edit .env with your Yahoo credentials

# Run Spring Boot
./gradlew bootRun
```

Backend runs at `https://localhost:8443`

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `https://localhost:5173` (proxies to backend)

## Project Structure

```
.
├── src/main/java/com/example/fbyahoo/
│   ├── config/           # Spring Security, CORS, WebClient
│   ├── controller/
│   │   ├── api/          # REST API endpoints
│   │   └── ingestion/    # Yahoo API ingestion triggers
│   ├── service/
│   │   ├── ingestion/    # Yahoo Fantasy API services
│   │   └── TokenService.java  # OAuth token management
│   ├── model/            # JPA entities
│   ├── repo/             # Spring Data repositories
│   └── dto/api/          # API response DTOs
├── src/main/resources/
│   ├── db/migration/     # Flyway SQL migrations
│   └── application.properties
├── frontend/
│   ├── src/
│   │   ├── api/          # API client
│   │   ├── components/   # React components
│   │   ├── context/      # Auth context
│   │   ├── pages/        # Route pages
│   │   └── types/        # TypeScript types
│   └── vite.config.ts
├── docker-compose.yml
├── Dockerfile            # Multi-stage build (frontend + backend)
└── DOCKER.md
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/auth/status` | Check authentication |
| GET | `/api/leagues` | List all leagues |
| GET | `/api/leagues/{key}` | League detail + my team |
| GET | `/api/leagues/{key}/roster` | My roster with stats |
| GET | `/api/leagues/{key}/available` | Top available players |
| GET | `/api/leagues/{key}/standings` | League standings |
| GET | `/api/leagues/{key}/matchup` | Current matchup |
| GET | `/api/leagues/{key}/insights` | Team insights |
| POST | `/api/sync` | Sync all data from Yahoo |

## Yahoo Developer Setup

1. Go to https://developer.yahoo.com/apps/
2. Create new app
3. Set redirect URI: `https://localhost:8443/oauth/yahoo/callback`
4. Copy Client ID and Client Secret to `.env`

## Database Schema

- `oauth_token` - Single-row Yahoo OAuth token storage
- `league` - Yahoo Fantasy leagues (31 fields)
- `team` - Teams in leagues (manager info flattened)
- `player` - Global player registry (eligibility, injury, editorial data)
- `player_stats` - Season averages per game (PTS, REB, AST, etc.)
- `player_ownership` - Ownership % and weekly deltas
- `league_rostered_player` - Snapshot of rostered players per league
- `matchup` - Weekly matchup data
- `matchup_stat` - Category-by-category matchup results

Migrations in `src/main/resources/db/migration/`

## Key Patterns

### Token Management
- Single-row `oauth_token` table
- Auto-refresh with 30s expiry buffer
- Clears tokens on refresh failure (forces re-login)

### Ingestion Pattern
- Controller (GET) → Service (@Transactional)
- Fetch JSON via `yahooFantasyClient` with Bearer token
- Parse with `YahooJson` utility (null-safe extractors)
- Upsert via JPA `findById().orElseGet()` + `save()`

### Yahoo JSON Quirks
- Responses use arrays of singleton objects
- Must walk by key name to extract fields
- `YahooJson` utility provides `text()`, `intOrNull()`, `bigDecimalOrNull()`, etc.

### Frontend Auth Flow
- AuthContext checks `/api/auth/status` on mount
- ProtectedRoute guards `/leagues` and `/leagues/:key`
- 401 responses redirect to `/login`
- OAuth callback redirects to `returnTo` or `/leagues`

## Commands

```bash
# Backend
./gradlew bootRun               # Run Spring Boot
./gradlew compileJava           # Compile only
./gradlew test                  # Run tests
./gradlew bootJar               # Build JAR

# Frontend
cd frontend
npm run dev                     # Dev server (hot reload)
npm run build                   # Production build → src/main/resources/static/
npm run preview                 # Preview production build

# Docker
docker compose up --build       # Build and start all services
docker compose down -v          # Stop and remove volumes
docker compose logs -f app      # View app logs

# Database
docker compose up -d db         # Start only database
docker compose down -v          # Nuke database (clean slate)
```

## Troubleshooting

**OAuth redirect fails:**
- Verify redirect URI in Yahoo Developer Console: `https://localhost:8443/oauth/yahoo/callback`
- Check `.env` has correct `YAHOO_CLIENT_ID` and `YAHOO_CLIENT_SECRET`

**Sync button does nothing:**
- Check browser console for errors
- Ensure you're logged in (token exists in database)
- Check backend logs for ingestion errors

**Database connection fails:**
- Ensure PostgreSQL is running: `docker compose up -d db`
- Check connection params in `.env`

**SSL certificate errors:**
- Ensure `keystore.p12` exists in `secrets/` or `src/main/resources/`
- For local dev, generate with `mkcert localhost`

**Frontend build fails:**
- Node.js 22+ required for Vite 7
- Run `npm install` in `frontend/` directory

## License

Private project - Not licensed for redistribution

## Credits

Built with Claude Code by Anthropic
