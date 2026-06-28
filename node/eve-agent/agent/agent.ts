import { createOpenAI } from "@ai-sdk/openai";
import { defineAgent } from "eve";

// 通过 OpenAI 兼容接口直连智谱 GLM（国内访问稳定，不走 Vercel AI Gateway）。
// 凭据与模型名走环境变量，建议放在 .env（已被 .gitignore 忽略）：
//   ZHIPU_API_KEY —— 必填，智谱开放平台的 API key
//   ZHIPU_MODEL   —— 可选，模型 id，默认 glm-4.6
const zhipu = createOpenAI({
  baseURL: "https://open.bigmodel.cn/api/paas/v4",
  apiKey: process.env.ZHIPU_API_KEY,
  // 用 name 覆盖默认的 "openai"，标识为第三方 provider
  name: "zhipu",
});

export default defineAgent({
  // 走 chat completions 接口（智谱的 OpenAI 兼容端点仅支持 /chat/completions）
  model: zhipu.chat(process.env.ZHIPU_MODEL ?? "glm-5.1"),
  // 智谱 GLM 走 direct provider，eve 无法从 AI Gateway 解析其 context window，
  // 必须显式声明，否则 compaction 编译失败、agent 无法启动（官方 escape hatch）。
  // 200000 = 200K（glm-5.1 的 context window）；换模型时请按智谱文档核对实际值。
  modelContextWindowTokens: 200000,
});
