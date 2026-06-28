// eve 框架文档的定位与读取工具函数，供 agent/tools/ 下的工具复用。
import { existsSync, readdirSync, readFileSync } from "node:fs";
import { dirname, join, relative } from "node:path";
import { fileURLToPath } from "node:url";

// 文档目录相对于项目根的路径片段
const DOCS_REL = ["node_modules", "eve", "docs"];

// 从「当前文件位置」与 process.cwd() 两个起点向上搜索 node_modules/eve/docs。
// 同时覆盖两个场景：源码直接运行（import.meta.url 指向 agent/ 下源文件）
// 与 eve 编译后运行（import.meta.url 指向 .eve/ 下编译产物），后者靠 cwd 兜底。
export function findEveDocsDir(): string {
  const startDirs = new Set<string>();
  try {
    startDirs.add(dirname(fileURLToPath(import.meta.url)));
  } catch {
    // import.meta.url 不可用时忽略，仅依赖 cwd
  }
  startDirs.add(process.cwd());

  for (const start of startDirs) {
    let dir = start;
    for (let i = 0; i < 10; i++) {
      const candidate = join(dir, ...DOCS_REL);
      if (existsSync(candidate)) return candidate;
      const parent = dirname(dir);
      if (parent === dir) break; // 已到文件系统根
      dir = parent;
    }
  }
  throw new Error("Could not locate eve docs directory (node_modules/eve/docs).");
}

// 递归收集目录下所有 .md 文件，按路径排序，跳过隐藏目录。
export function collectMarkdown(root: string): string[] {
  const results: string[] = [];
  const walk = (dir: string): void => {
    for (const entry of readdirSync(dir, { withFileTypes: true })) {
      if (entry.name.startsWith(".")) continue;
      const full = join(dir, entry.name);
      if (entry.isDirectory()) {
        walk(full);
      } else if (entry.isFile() && entry.name.endsWith(".md")) {
        results.push(full);
      }
    }
  };
  walk(root);
  return results.sort();
}

// 将绝对路径转为文档 id：相对 docs 目录、正斜杠分隔、去掉 .md 扩展名。
// 例：.../node_modules/eve/docs/reference/project-layout.md -> reference/project-layout
export function toDocId(docsDir: string, full: string): string {
  return relative(docsDir, full).replace(/\\/g, "/").replace(/\.md$/, "");
}

// 极简 frontmatter 解析，仅提取 title 与 description 两个字段。
export function parseFrontmatter(
  content: string,
): { title?: string; description?: string } {
  const match = content.match(/^---\r?\n([\s\S]*?)\r?\n---/);
  if (!match) return {};
  const fm = match[1];
  const read = (key: string): string | undefined =>
    fm.match(new RegExp(`^${key}:\\s*"?(.*?)"?\\s*$`, "m"))?.[1];
  return { title: read("title"), description: read("description") };
}

// 以 utf8 读取文件全文。
export function readText(full: string): string {
  return readFileSync(full, "utf8");
}
