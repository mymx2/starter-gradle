#!/bin/sh
echo "🚀 Pre-push check start"

# Prettier formatting check (vp > pnpm > skip; remote CI is the gate)
if command -v vp >/dev/null 2>&1; then
    vp run check
elif command -v pnpm >/dev/null 2>&1; then
    echo "ℹ️  'vp' not found, falling back to 'pnpm run check'"
    pnpm run check
else
    echo "⚠️  Neither 'vp' nor 'pnpm' found, skipping Prettier check (remote CI will verify)"
fi
if [ $? -ne 0 ]; then
    echo "❌ Prettier format check failed. Run 'pnpm run fmt' to fix."
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
