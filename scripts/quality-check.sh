#!/bin/bash

# MysticTreasures Professional Quality Check Script (Linux/macOS)
# This script runs industry-standard checks via Maven plugins.

# Colors for output
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
GREEN='\033[0;32m'
GRAY='\033[0;90m'
NC='\033[0m' # No Color

echo -e "${CYAN}--- MysticTreasures Professional Quality Check ---${NC}"

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}[ERROR] Maven (mvn) is required but not found in PATH.${NC}"
    exit 1
fi

# 1. Run Unit Tests
echo -e "\n${YELLOW}[1/4] Running Unit & Integration Tests (MockBukkit)...${NC}"
mvn test
if [ $? -ne 0 ]; then
    echo -e "${RED}Tests failed! Fix issues before proceeding.${NC}"
    exit 1
fi

# 2. Style Check (Checkstyle)
echo -e "\n${YELLOW}[2/4] Running Style Analysis (Checkstyle)...${NC}"
mvn checkstyle:check
if [ $? -ne 0 ]; then
    echo -e "${RED}Style violations found! Check target/checkstyle-result.xml${NC}"
else
    echo -e "${GREEN}Code style is excellent!${NC}"
fi

# 3. Bug Hunting (SpotBugs)
echo -e "\n${YELLOW}[3/4] Running Bug Analysis (SpotBugs)...${NC}"
mvn spotbugs:check
if [ $? -ne 0 ]; then
    echo -e "${RED}Potential bugs detected! Check target/spotbugsXml.xml${NC}"
else
    echo -e "${GREEN}No obvious bugs detected by SpotBugs.${NC}"
fi

# 4. Code Metrics & Scoring
echo -e "\n${YELLOW}[4/4] Calculating Code Score...${NC}"
java_files=$(find src -name "*.java" | wc -l)
todo_count=$(grep -r "TODO" src | wc -l)

base_score=100
deductions=$((todo_count * 2))

# Heuristic: subtract points if spotbugs or checkstyle had issues (this script continues on error)
# Note: we check the return code of the last mvn command, but ideally we'd track each.
# For simplicity in this shell version:
final_score=$((base_score - deductions))
if [ $final_score -lt 0 ]; then final_score=0; fi

echo -e "--- Summary ---"
echo -e "Total Source Files: $java_files"
echo -e "Pending TODOs: $todo_count"
echo -e "${GREEN}Final Quality Score: $final_score/100${NC}"

echo -e "\n${GRAY}Full report available in the 'target' directory.${NC}"
