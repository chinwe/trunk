import { defineTool } from "eve/tools";
import { z } from "zod";
import {
  collectMarkdown,
  findEveDocsDir,
  parseFrontmatter,
  readText,
  toDocId,
} from "../lib/eve-docs.js";

export default defineTool({
  description:
    "List every eve framework documentation file bundled with the installed eve package. " +
    "Returns each document's path (relative to the docs root, without the .md extension), " +
    "its title, and a one-line description. Call this first to discover what documentation " +
    "exists, then use read_eve_doc with one of the returned paths to read its full content.",
  inputSchema: z.object({}),
  async execute() {
    const docsDir = findEveDocsDir();
    const files = collectMarkdown(docsDir);
    const docs = files.map((full) => {
      const path = toDocId(docsDir, full);
      const { title, description } = parseFrontmatter(readText(full));
      return { path, title: title ?? path, description: description ?? null };
    });
    return { count: docs.length, docs };
  },
});
