import { defineChannel, GET } from "eve/channels";
// 复用 package.json 既有的 #* -> ./agent/* 导入映射
import { WEB_CHAT_HTML } from "#lib/web-chat-page.js";

// 自定义 channel：仅用一个 GET 路由把单页 HTML 返回给浏览器。
// 聊天数据走同源的内置 /eve/v1/* 路由（eve channel），这里只托管页面本身。
export default defineChannel({
  routes: [
    GET("/chat", async () => {
      return new Response(WEB_CHAT_HTML, {
        headers: { "content-type": "text/html; charset=utf-8" },
      });
    }),
  ],
});
