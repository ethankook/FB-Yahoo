#!/bin/bash

echo "🏀 Fantasy Basketball Helper - Docker Setup"
echo ""

# Check if .env exists
if [ ! -f .env ]; then
    echo "❌ .env file not found!"
    echo "📝 Creating .env from .env.example..."
    cp .env.example .env
    echo "✅ .env created. Please edit it with your Yahoo credentials:"
    echo "   - YAHOO_CLIENT_ID"
    echo "   - YAHOO_CLIENT_SECRET"
    echo ""
    echo "Then run: ./start.sh"
    exit 1
fi

# Check if secrets directory exists
if [ ! -d secrets ]; then
    echo "📁 Creating secrets directory..."
    mkdir -p secrets
fi

# Check if keystore exists
if [ ! -f secrets/keystore.p12 ] && [ ! -f src/main/resources/keystore.p12 ]; then
    echo "⚠️  Warning: No SSL keystore found!"
    echo "   Expected at: secrets/keystore.p12"
    echo "   The app may fail to start without SSL certificates."
    echo ""
fi

# Copy keystore if it exists in resources but not in secrets
if [ ! -f secrets/keystore.p12 ] && [ -f src/main/resources/keystore.p12 ]; then
    echo "📋 Copying keystore to secrets directory..."
    cp src/main/resources/keystore.p12 secrets/
fi

echo "🚀 Starting services with Docker Compose..."
echo ""

docker compose up --build

