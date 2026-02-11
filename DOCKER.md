# Docker Setup

This guide explains how to run the entire Fantasy Basketball Helper stack using Docker Compose.

## Prerequisites

- Docker Desktop installed
- Yahoo Developer App credentials (Client ID, Client Secret)
- SSL certificates in `secrets/` directory

## Quick Start

1. **Set up environment variables**

Create a `.env` file in the project root:

```bash
# Database
POSTGRES_DB=fbyahoo
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password

# Yahoo OAuth
YAHOO_CLIENT_ID=your_yahoo_client_id
YAHOO_CLIENT_SECRET=your_yahoo_client_secret
YAHOO_REDIRECT_URI=https://localhost:8443/oauth/yahoo/callback
```

2. **Ensure SSL certificates exist**

Make sure you have `keystore.p12` in `secrets/` directory:

```bash
mkdir -p secrets
# Copy your keystore.p12 to secrets/
cp src/main/resources/keystore.p12 secrets/
```

3. **Start everything**

```bash
docker compose up --build
```

This will:
- Build the frontend (React + Vite)
- Build the backend (Spring Boot + Java 21)
- Start PostgreSQL
- Run Flyway migrations
- Serve the app at `https://localhost:8443`

## Usage

1. Open browser: `https://localhost:8443`
2. Click "Login with Yahoo"
3. Authorize the app
4. Click "Sync Data" to pull your leagues
5. Click any league to view dashboard

## Commands

```bash
# Start all services
docker compose up

# Start in detached mode
docker compose up -d

# Rebuild and start
docker compose up --build

# Stop all services
docker compose down

# Stop and remove volumes (clean slate)
docker compose down -v

# View logs
docker compose logs -f app

# Restart just the app
docker compose restart app
```

## Development vs Production

### Development Mode (Hot Reload)

For active development with hot reload:

```bash
# Terminal 1 - Database
docker compose up db

# Terminal 2 - Backend
./gradlew bootRun

# Terminal 3 - Frontend
cd frontend
npm run dev
```

Frontend: `https://localhost:5173` (proxies to backend)
Backend: `https://localhost:8443`

### Production Mode (Docker)

For production-like deployment:

```bash
docker compose up --build
```

Everything runs in Docker: `https://localhost:8443`

## Troubleshooting

**Build fails with "Node version" error**
- The Dockerfile uses Node 22, which is compatible with Vite 7
- If you see warnings, they're safe to ignore

**SSL certificate errors**
- Ensure `secrets/keystore.p12` exists and is valid
- Volume mount: `./secrets:/app/secrets:ro`

**Database connection errors**
- Wait for healthcheck: `docker compose logs db`
- Check environment variables in `.env`

**Port already in use**
- Stop local services: `./gradlew --stop`
- Kill port 8443: `lsof -ti:8443 | xargs kill -9`

**OAuth redirect fails**
- Verify `YAHOO_REDIRECT_URI=https://localhost:8443/oauth/yahoo/callback`
- Update Yahoo Developer Console with this exact URI

## Architecture

```
┌─────────────────────────────────────────┐
│  Browser (https://localhost:8443)      │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Docker Container: fb_helper_app        │
│  ┌─────────────────────────────────┐   │
│  │  Spring Boot (Port 8443)        │   │
│  │  - Serves React SPA (/)         │   │
│  │  - REST API (/api/**)           │   │
│  │  - OAuth Flow (/oauth/yahoo/**) │   │
│  └─────────────────────────────────┘   │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Docker Container: fb_helper_db         │
│  PostgreSQL 16 (Port 5432)              │
│  - Persistent volume: pgdata            │
└─────────────────────────────────────────┘
```

## Multi-Stage Build

The Dockerfile uses a 3-stage build:

1. **Stage 1**: Build frontend (Node.js)
   - npm ci
   - npm run build → outputs to `src/main/resources/static/`

2. **Stage 2**: Build backend (Java 21 JDK)
   - Copy frontend build from stage 1
   - ./gradlew bootJar

3. **Stage 3**: Runtime (Java 21 JRE)
   - Copy JAR from stage 2
   - Minimal runtime image (~200MB)

## Environment Variables Reference

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_DB` | `fbyahoo` | Database name |
| `POSTGRES_USER` | `postgres` | Database user |
| `POSTGRES_PASSWORD` | `postgres` | Database password |
| `POSTGRES_HOST` | `db` | Database host (Docker service name) |
| `POSTGRES_PORT` | `5432` | Database port |
| `YAHOO_CLIENT_ID` | *required* | Yahoo Developer App Client ID |
| `YAHOO_CLIENT_SECRET` | *required* | Yahoo Developer App Client Secret |
| `YAHOO_REDIRECT_URI` | `https://localhost:8443/oauth/yahoo/callback` | OAuth redirect URI |
| `SPRING_PROFILES_ACTIVE` | `prod` | Spring Boot profile |

## Clean Rebuild

If you need to completely rebuild:

```bash
# Stop and remove everything
docker compose down -v

# Remove build cache
docker builder prune -a

# Rebuild from scratch
docker compose build --no-cache
docker compose up
```
