#!/bin/sh
# ============================================
# Git pre-commit hook
# 检查本次提交的新增代码行中是否包含敏感关键字
# ============================================

PART1="TO"
PART2="DY"
KEYWORDS=("${PART1}${PART2}")

# 获取暂存区的 diff（仅新增行，忽略删除和上下文）
DIFF_CONTENT=$(git diff --cached --unified=0 | grep -E "^\+" | grep -vE "^\+\+\+")

[ -z "$DIFF_CONTENT" ] && exit 0

HAS_FORBIDDEN=false

# 遍历关键字逐一检查
for KEY in "${KEYWORDS[@]}"; do
  echo "$DIFF_CONTENT" | grep -i --color=never "$KEY" >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "❌ 检测到新增代码行中包含敏感关键字: '$KEY'"
    HAS_FORBIDDEN=true
  fi
done

if [ "$HAS_FORBIDDEN" = true ]; then
  echo "🚫 提交已被阻止，请删除敏感内容后再提交。"
  exit 1
fi

echo "✅ pre-commit 检查通过。"
exit 0
