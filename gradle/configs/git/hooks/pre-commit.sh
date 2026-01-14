#!/bin/sh
# ============================================
# Git pre-commit hook
# 检查本次提交的新增代码行中是否包含敏感关键字
# ============================================

PART1="TO"
PART2="DY"
KEYWORDS="${PART1}${PART2}"

# 获取暂存区新增行
DIFF_CONTENT=$(git diff --cached --unified=0 | grep '^+' | grep -v '^+++')

[ -z "$DIFF_CONTENT" ] && exit 0

HAS_FORBIDDEN=0

for KEY in $KEYWORDS; do
  echo "$DIFF_CONTENT" | grep -i "$KEY" >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "❌ ERROR: detected forbidden keyword in added lines: '$KEY'"
    HAS_FORBIDDEN=1
  fi
done

if [ $HAS_FORBIDDEN -ne 0 ]; then
  echo "🚫 Commit blocked: please remove sensitive content."
  exit 1
fi

echo "✅ pre-commit check passed."
exit 0
