// Web Chat 单页应用：完整的 HTML+CSS+JS 作为一个字符串常量。
// 由 web-chat channel 的 GET /chat 路由返回给浏览器。
// 事件字段名依据实测的 eve NDJSON 流（messageSoFar/messageDelta/message、
// actions[].callId/toolName/input、result.callId/output）。
// reasoning 字段名 GLM 当前不产出该事件、无法实测，按 message 系列做降级兼容（见 spec §11）。
export const WEB_CHAT_HTML: string = `<!doctype html>
<html lang="zh">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>eve-agent · Web Chat</title>
  <style>
    :root { color-scheme: light dark; --bubble-user: #2563eb; --bubble-assistant: #1f2937; }
    * { box-sizing: border-box; }
    body { margin: 0; font: 15px/1.5 system-ui, sans-serif; background: #0b1020; color: #e5e7eb; }
    main { max-width: 760px; margin: 0 auto; display: flex; flex-direction: column; height: 100vh; padding: 16px; gap: 12px; }
    header h1 { margin: 0; font-size: 18px; }
    header p { margin: 2px 0 0; color: #9ca3af; font-size: 12px; }
    #messages { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 10px; }
    .msg { padding: 10px 12px; border-radius: 10px; white-space: pre-wrap; word-break: break-word; }
    .msg.user { align-self: flex-end; background: var(--bubble-user); color: #fff; }
    .msg.assistant { align-self: flex-start; background: var(--bubble-assistant); }
    .msg .role { font-size: 11px; opacity: .6; margin-bottom: 4px; }
    .tool { align-self: flex-start; background: #0f172a; border: 1px solid #334155; border-radius: 10px; padding: 8px 10px; font-size: 13px; max-width: 100%; }
    .tool .name { color: #93c5fd; font-weight: 600; }
    .tool .args { color: #cbd5e1; margin-top: 4px; white-space: pre-wrap; }
    .tool .result { color: #86efac; margin-top: 4px; white-space: pre-wrap; }
    .tool .pending { color: #fbbf24; }
    .reasoning { align-self: flex-start; background: #1e1b4b; border-left: 3px solid #6366f1; border-radius: 8px; padding: 8px 10px; font-size: 13px; color: #c7d2fe; white-space: pre-wrap; }
    .reasoning .role { font-size: 11px; opacity: .7; margin-bottom: 4px; }
    #error { color: #fca5a5; background: #7f1d1d33; padding: 8px 10px; border-radius: 8px; }
    form { display: flex; gap: 8px; }
    input { flex: 1; padding: 10px; border-radius: 8px; border: 1px solid #374151; background: #111827; color: inherit; }
    button { padding: 10px 16px; border: 0; border-radius: 8px; background: var(--bubble-user); color: #fff; cursor: pointer; }
    button:disabled { opacity: .5; cursor: default; }
  </style>
</head>
<body>
  <main>
    <header>
      <h1>eve-agent</h1>
      <p>eve framework learning assistant · web chat</p>
    </header>
    <div id="messages"></div>
    <div id="error" hidden></div>
    <form id="chat-form">
      <input id="input" placeholder="Ask about the eve framework..." autocomplete="off" />
      <button id="send" type="submit">Send</button>
    </form>
  </main>
  <script>
    // --- 会话状态 ---
    const state = {
      sessionId: null,           // durable session id（首条 POST 响应拿到）
      continuationToken: null,   // 续发凭证（每次响应更新）
      streamIndex: 0,            // 已消费的事件计数，重连时用 startIndex 续读
      status: "ready",           // ready | streaming
    };

    const messagesEl = document.getElementById("messages");
    const errorEl = document.getElementById("error");
    const form = document.getElementById("chat-form");
    const input = document.getElementById("input");
    const sendBtn = document.getElementById("send");

    // 当前助手气泡 / 思考块的文本节点；事件无 id，故按"当前轮"复用单例
    let currentAssistantText = null;
    let currentReasoningText = null;

    // --- 渲染辅助 ---
    function scrollDown() { messagesEl.scrollTop = messagesEl.scrollHeight; }

    function appendUser(text) {
      const el = document.createElement("div");
      el.className = "msg user";
      el.textContent = text;
      messagesEl.appendChild(el);
      scrollDown();
    }

    function createAssistantBubble() {
      const el = document.createElement("div");
      el.className = "msg assistant";
      const role = document.createElement("div");
      role.className = "role";
      role.textContent = "assistant";
      const text = document.createElement("div");
      text.className = "text";
      el.appendChild(role);
      el.appendChild(text);
      messagesEl.appendChild(el);
      scrollDown();
      return text;
    }

    function safeJson(v) {
      try { return typeof v === "string" ? v : JSON.stringify(v, null, 2); }
      catch { return String(v); }
    }

    // 工具调用卡片：用 callId 关联 actions.requested 与 action.result
    function appendToolCall(callId, name, args) {
      const el = document.createElement("div");
      el.className = "tool";
      el.id = "tool-" + callId;
      const n = document.createElement("div"); n.className = "name"; n.textContent = "🔧 " + (name || "tool"); el.appendChild(n);
      const a = document.createElement("div"); a.className = "args"; a.textContent = "args: " + safeJson(args); el.appendChild(a);
      const r = document.createElement("div"); r.className = "result pending"; r.textContent = "…"; el.appendChild(r);
      // 插到当前助手气泡之前，体现"先调工具再回答"的时序
      const before = currentAssistantText ? currentAssistantText.parentElement : null;
      if (before) messagesEl.insertBefore(el, before);
      else messagesEl.appendChild(el);
      scrollDown();
    }

    // 思考块（reasoning）；GLM 当前不产出 reasoning 事件时不会出现（spec §11）
    function ensureReasoning() {
      if (currentReasoningText) return currentReasoningText;
      const el = document.createElement("div");
      el.className = "reasoning";
      const role = document.createElement("div"); role.className = "role"; role.textContent = "reasoning"; el.appendChild(role);
      const text = document.createElement("div"); text.className = "text"; el.appendChild(text);
      const before = currentAssistantText ? currentAssistantText.parentElement : null;
      if (before) messagesEl.insertBefore(el, before);
      else messagesEl.appendChild(el);
      currentReasoningText = text;
      scrollDown();
      return text;
    }

    function showError(message) {
      errorEl.textContent = message;
      errorEl.hidden = false;
    }
    function clearError() { errorEl.hidden = true; errorEl.textContent = ""; }

    function setStatus(next) {
      state.status = next;
      const busy = next === "streaming";
      input.disabled = busy;
      sendBtn.disabled = busy;
    }

    // --- 发送消息 ---
    async function sendMessage(text) {
      clearError();
      appendUser(text);
      currentAssistantText = createAssistantBubble();
      currentReasoningText = null; // 每轮重置思考块
      setStatus("streaming");

      // 首条走 /eve/v1/session；续发走 /eve/v1/session/<id> 并带 continuationToken
      const url = state.sessionId
        ? "/eve/v1/session/" + encodeURIComponent(state.sessionId)
        : "/eve/v1/session";
      const body = state.sessionId
        ? { continuationToken: state.continuationToken, message: text }
        : { message: text };

      let res;
      try {
        res = await fetch(url, {
          method: "POST",
          headers: { "content-type": "application/json" },
          body: JSON.stringify(body),
        });
      } catch (err) {
        showError("Network error: " + err.message);
        setStatus("ready");
        return;
      }
      if (!res.ok) {
        showError("HTTP " + res.status);
        setStatus("ready");
        return;
      }
      const data = await res.json().catch(() => ({}));
      if (data.sessionId) state.sessionId = data.sessionId;
      if (data.continuationToken) state.continuationToken = data.continuationToken;

      // 首次建立长连接 stream；续发复用同一条连接（durable stream 不主动 EOF）
      if (!readerActive) openStream();
    }

    // --- 读取 NDJSON 事件流（长连接，靠事件类型判定边界）---
    let readerActive = false;
    async function openStream() {
      if (readerActive) return;
      readerActive = true;
      try {
        const url = "/eve/v1/session/" + encodeURIComponent(state.sessionId)
          + "/stream?startIndex=" + state.streamIndex;
        const res = await fetch(url);
        if (!res.ok || !res.body) { showError("Stream HTTP " + res.status); return; }
        const reader = res.body.getReader();
        const decoder = new TextDecoder();
        let buffer = "";
        while (true) {
          const { value, done } = await reader.read();
          if (done) break;
          buffer += decoder.decode(value, { stream: true });
          let nl;
          while ((nl = buffer.indexOf("\\n")) >= 0) {
            const line = buffer.slice(0, nl).trim();
            buffer = buffer.slice(nl + 1);
            if (!line) continue;
            let event;
            try { event = JSON.parse(line); }
            catch { continue; } // 跳过不完整的行
            handleEvent(event);
          }
        }
      } catch (err) {
        showError("Stream error: " + err.message);
      } finally {
        readerActive = false;
      }
    }

    // --- 事件分发（字段名依据实测）---
    function handleEvent(event) {
      const d = event.data || {};
      switch (event.type) {
        case "message.appended":
          if (currentAssistantText) {
            if (typeof d.messageSoFar === "string") currentAssistantText.textContent = d.messageSoFar;
            else if (typeof d.messageDelta === "string") currentAssistantText.textContent += d.messageDelta;
          }
          break;
        case "message.completed":
          if (currentAssistantText && typeof d.message === "string") {
            currentAssistantText.textContent = d.message;
          }
          break;
        case "actions.requested": {
          // 实测字段：data.actions[] 每项含 callId / toolName / input
          const actions = Array.isArray(d.actions) ? d.actions : [];
          for (const c of actions) {
            const id = c.callId || c.id;
            if (id) appendToolCall(id, c.toolName || c.name, c.input);
          }
          break;
        }
        case "action.result": {
          // 实测字段：data.result 含 callId / output
          const r = d.result || {};
          const id = r.callId || d.callId || d.id;
          const cell = id ? document.querySelector("#tool-" + id + " .result") : null;
          if (cell) {
            cell.className = "result";
            cell.textContent = "result: " + safeJson(r.output ?? d.output);
          }
          break;
        }
        case "reasoning.appended": {
          // GLM 当前不产出 reasoning 事件（spec §11），字段名按 message 系列降级兼容
          const t = ensureReasoning();
          if (typeof d.reasoningSoFar === "string") t.textContent = d.reasoningSoFar;
          else if (typeof d.messageSoFar === "string") t.textContent = d.messageSoFar;
          else if (typeof d.reasoningDelta === "string") t.textContent += d.reasoningDelta;
          else if (typeof d.messageDelta === "string") t.textContent += d.messageDelta;
          break;
        }
        case "reasoning.completed": {
          const t = ensureReasoning();
          if (typeof d.reasoning === "string") t.textContent = d.reasoning;
          else if (typeof d.message === "string") t.textContent = d.message;
          break;
        }
        case "session.waiting":
        case "session.completed":
          setStatus("ready");
          break;
        case "step.failed":
        case "turn.failed":
        case "session.failed":
          showError(d.message || event.type);
          setStatus("ready");
          break;
        default:
          break;
      }
      state.streamIndex += 1;
      scrollDown();
    }

    // --- 绑定输入 ---
    form.addEventListener("submit", (e) => {
      e.preventDefault();
      const text = input.value.trim();
      if (!text || state.status === "streaming") return;
      input.value = "";
      void sendMessage(text);
    });
  <\/script>
</body>
</html>`;
