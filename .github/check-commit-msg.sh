#!/usr/bin/env bash
set -e

echo "🔍 Checking commit messages..."

COMMITS=$(git log --format=%s "${GITHUB_EVENT_BEFORE:-HEAD^}".."${GITHUB_SHA:-HEAD}")
COMMIT_MSG_PATTERN='^(?:revert: )?(feat|fix|refactor|perf|test|infra|deps|docs|chore|wip|release)(\(.+\))?: [^\n\r]{1,49}[^\s\n\r]$'

# ANSI colors
RESET="\033[0m"
RED="\033[31m"
GREEN="\033[32m"
BLUE="\033[34m"
BG_RED="\033[41m"

echo "$COMMITS" | while read -r COMMIT_MSG; do
  if ! echo "$COMMIT_MSG" | grep -Eq "$COMMIT_MSG_PATTERN"; then
    echo -e "${BG_RED}ERROR${RESET}  ${RED}invalid commit message format.${RESET}\n"
    echo -e "${RED}Proper commit message format is required for automated changelog generation. Examples:${RESET}\n"
    echo -e "  ${GREEN}feat(compiler): add 'comments' option${RESET}"
    echo -e "  ${GREEN}fix(v-model): handle events on blur (close #28)${RESET}\n"
    echo -e "${RED}Commit message header: <type>(<scope>): <subject>${RESET}"
    echo -e "${RED}Commit message header pattern: ${COMMIT_MSG_PATTERN}${RESET}"
    echo -e "${RED}See${RESET} ${BLUE}https://github.com/conventional-commits/conventionalcommits.org${RESET} ${RED}for more details.${RESET}\n"
    echo -e "${RED}❌ Invalid commit message:${RESET} '${COMMIT_MSG}'"
    exit 1
  fi
done

echo -e "${GREEN}✅ All commit messages are valid.${RESET}"
