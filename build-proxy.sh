#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo "Building update proxy..."
cd updates
go build -o build/turtlebrowseupdates .
echo -e "${GREEN}Successfully built update proxy!${NC}"
cd ..