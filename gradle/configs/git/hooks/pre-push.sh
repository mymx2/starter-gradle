#!/bin/sh
echo "🚀 Pre-push check start"

# Prettier formatting check (via pnpm, not Gradle)
pnpm run format:check
if [ $? -ne 0 ]; then
    echo "❌ Prettier format check failed. Run 'pnpm run format' to fix."
    exit 1
fi

# Gradle check (spotless, tests, etc.)
./gradlew check -PSKIP_ALL_LOCAL=false
if [ $? -eq 0 ]; then
    echo "✅ Pre-push check passed"
else
    echo "❌ Pre-push check failed"
    exit 1
fi
