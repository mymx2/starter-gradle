#!/bin/sh
set -e

echo "🚀 Pre-push check start"

./gradlew check

if [ $? -eq 0 ]; then
    echo "✅ Pre-push check passed"
else
    echo "❌ Pre-push check failed"
    exit 1
fi
