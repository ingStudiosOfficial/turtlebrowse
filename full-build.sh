#!/bin/bash

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${GREEN}Turtlebrowse Full Build CLI${NC}"
echo -e "${GREEN}----------------------------${NC}"
echo ""

echo "Pulling latest changes..."
git pull
echo ""

./build-frontend.sh
echo ""

./build-proxy.sh
echo ""

echo "Running Gradle wrapper build script..."
./gradlew build
echo ""

echo -e "${GREEN}🎉 Successfully built Turtlebrowse! 🎉${NC}"