#!/bin/sh

echo "🚀 Pre-push check start"

# 推送前必须跑完整 check：强制关闭所有本地 SKIP_* 门控，
# 避免开发者本地 gradle.properties / local.properties 里的 SKIP_* 泄漏到推送校验，
# 否则跳过的质量 / 覆盖率 / e2e / 文档 / 集成测试会静默进入主干。
# -P 优先级高于 gradle.properties（见 gradle/build-logic/.../ProjectExtensions.kt 的属性加载优先级注释），
# 因此这里显式置 false 可覆盖任何本地 SKIP_* 设置。
# 设计依据：docs/engineering/perf-skip-quality-decoupling.md（"推送前务必跑一次完整 check"）。
./gradlew check \
  -PSKIP_QUALITY=false \
  -PSKIP_COVERAGE=false \
  -PSKIP_E2E=false \
  -PSKIP_DOC=false \
  -PSKIP_INTEGRATION=false \
  -PSKIP_ALL_LOCAL=false

if [ $? -eq 0 ]; then
    echo "✅ Pre-push check passed"
else
    echo "❌ Pre-push check failed"
    exit 1
fi
