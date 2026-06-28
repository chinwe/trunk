import { existsSync } from "node:fs";
import { join } from "node:path";
import { defineTool } from "eve/tools";
import { z } from "zod";
import {
  collectMarkdown,
  findEveDocsDir,
  readText,
  toDocId,
} from "../lib/eve-docs.js";

export default defineTool({
  description:
    "Read the full content of a single eve framework documentation file. " +
    "Accepts a doc path (e.g. 'reference/project-layout') or a file name (e.g. 'agent-config'). " +
    "Use list_eve_docs first to see the available paths.",
  inputSchema: z.object({
    name: z
      .string()
      .describe(
        "Doc path such as 'reference/project-layout', or file name such as 'agent-config'",
      ),
  }),
  async execute({ name }) {
    const docsDir = findEveDocsDir();
    // 规范化输入：去 .md 后缀、统一正斜杠、去 docs/ 前缀
    const normalized = name
      .replace(/\.md$/i, "")
      .replace(/\\/g, "/")
      .replace(/^docs\//i, "");

    // 1) 直接按相对路径匹配
    const direct = join(docsDir, `${normalized}.md`);
    if (existsSync(direct)) {
      return { path: normalized, content: readText(direct) };
    }

    // 2) 按文件名（basename）模糊匹配，兜底用户只给了文档名的情况
    const match = collectMarkdown(docsDir).find((full) => {
      const id = toDocId(docsDir, full);
      const base = id.split("/").pop();
      return id === normalized || base === normalized || id.endsWith(`/${normalized}`);
    });
    if (match) {
      return { path: toDocId(docsDir, match), content: readText(match) };
    }

    return {
      error: `Document not found: '${name}'. Call list_eve_docs to see available documents.`,
    };
  },
});
