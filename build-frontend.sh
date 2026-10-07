#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo "Building internal pages..."
cd frontend/pages
npm install
npm run build
cd ../..
echo ""

echo "Building Dino game..."
cd frontend/games/dino
npm install
npm run build
cd ../..
echo ""

echo -e "${GREEN}Successfully built frontend!${NC}"