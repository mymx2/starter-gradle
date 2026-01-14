@file:Suppress("UnstableApiUsage", "PropertyName")

import io.github.mymx2.plugin.utils.Ansi
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated

val commitMsgFile = ".git/hooks/commit-msg"
val preCommitFile = ".git/hooks/pre-commit"
val prePushFile = ".git/hooks/pre-push"
val commitMsgSh = "gradle/configs/git/hooks/hooks/commit-msg.sh"
val preCommitFileSh = "gradle/configs/git/hooks/hooks/pre-commit.sh"
val prePushSh = "gradle/configs/git/hooks/hooks/pre-push.sh"

// only root project
if (path == ":") {
  initPreCommitHook()
  initCommitMsgHook()
  initPrePushHook()
}

fun initPreCommitHook() {
  val rootDir = isolated.rootProject.projectDirectory

  // see: https://git-scm.com/book/zh/v2/%e8%87%aa%e5%ae%9a%e4%b9%89-Git-Git-%e9%92%a9%e5%ad%90
  val preCommitHook = rootDir.file(preCommitFile).asFile

  if (!preCommitHook.exists()) {
    val preCommitFileSh = rootDir.file(preCommitFileSh).asFile
    if (preCommitFileSh.exists()) {
      preCommitHook.writeText(preCommitFileSh.readText())
    } else {
      preCommitHook.ensureParentDirsCreated()
      preCommitHook.writeText(
        $$"""
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

        """
          .trimIndent()
      )
    }
    logger.lifecycle(
      Ansi.color(
        "✓ Git hooks: 'pre-commit' installed successfully to ${preCommitHook.path}",
        Ansi.Color.GREEN.code,
      )
    )
  }
}

fun initCommitMsgHook() {
  val rootDir = isolated.rootProject.projectDirectory
  val commitMsgHook = rootDir.file(commitMsgFile).asFile

  if (!commitMsgHook.exists()) {
    val commitMsgSh = rootDir.file(commitMsgSh).asFile
    if (commitMsgSh.exists()) {
      commitMsgHook.writeText(commitMsgSh.readText())
    } else {
      commitMsgHook.ensureParentDirsCreated()
      commitMsgHook.writeText(
        """
        #!/bin/sh
        set -e

        ./.github/check-commit-editmsg.sh
        """
          .trimIndent()
      )
    }
    logger.lifecycle(
      Ansi.color(
        "✓ Git hooks: 'commit-msg' installed successfully to ${commitMsgHook.path}",
        Ansi.Color.GREEN.code,
      )
    )
  }
}

fun initPrePushHook() {
  val rootDir = isolated.rootProject.projectDirectory

  // see: https://git-scm.com/book/zh/v2/%e8%87%aa%e5%ae%9a%e4%b9%89-Git-Git-%e9%92%a9%e5%ad%90
  val prePushHook = rootDir.file(prePushFile).asFile

  if (!prePushHook.exists()) {
    val prePushSh = rootDir.file(prePushSh).asFile
    if (prePushSh.exists()) {
      prePushHook.writeText(prePushSh.readText())
    } else {
      prePushHook.ensureParentDirsCreated()
      prePushHook.writeText(
        """
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
        """
          .trimIndent()
      )
    }
    logger.lifecycle(
      Ansi.color(
        "✓ Git hooks: 'pre-push' installed successfully to ${prePushHook.path}",
        Ansi.Color.GREEN.code,
      )
    )
  }
}
