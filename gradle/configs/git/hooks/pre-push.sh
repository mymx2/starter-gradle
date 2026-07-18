#!/bin/sh
echo "🚀 Pre-push check start"
./gradlew check -PSKIP_ALL_LOCAL=false
if [ $? -eq 0 ]; then
    echo "✅ Pre-push check passed"
else
    echo "❌ Pre-push check failed"
    exit 1
fi
