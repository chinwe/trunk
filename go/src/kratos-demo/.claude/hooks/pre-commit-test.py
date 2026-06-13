#!/usr/bin/env python3
"""PreToolUse hook：拦截 git commit，提交前自动运行 go test，失败则阻止提交（exit 2）。"""
import json
import os
import re
import subprocess
import sys


def main():
    # 读取 hook 事件 JSON
    try:
        data = json.load(sys.stdin)
    except (json.JSONDecodeError, ValueError):
        sys.exit(0)  # 输入异常时不阻塞工具调用

    # 只关心 Bash 工具
    if data.get("tool_name") != "Bash":
        sys.exit(0)

    command = data.get("tool_input", {}).get("command", "")
    if not command:
        sys.exit(0)

    # 只拦截 git commit 命令，其余放行
    if not re.search(r"\bgit\s+commit\b", command):
        sys.exit(0)

    # 命中 commit，跑测试
    project_dir = os.environ.get("CLAUDE_PROJECT_DIR", ".")
    print("[pre-commit] running go test ./...", file=sys.stderr)
    result = subprocess.run(
        ["go", "test", "./..."],
        cwd=project_dir,
        capture_output=True,
        text=True,
    )
    sys.stderr.write(result.stdout)
    if result.returncode != 0:
        sys.stderr.write(result.stderr)
        print("[pre-commit] BLOCKED: go test failed. Fix tests before committing.", file=sys.stderr)
        sys.exit(2)

    print("[pre-commit] go test passed.", file=sys.stderr)
    sys.exit(0)


if __name__ == "__main__":
    main()
