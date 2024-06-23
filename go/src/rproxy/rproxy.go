package main

import (
	"bytes"
	"crypto/tls"
	"io"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
)

func main() {

	allowMethods := map[string]bool{
		"OPTIONS": true,
		"HEAD":    true,
		"GET":     true,
		"POST":    true,
	}

	targetURL, _ := url.Parse("https://backend-service")
	proxy := httputil.NewSingleHostReverseProxy(targetURL)

	// 忽略目标服务器的TLS证书
	proxy.Transport = &http.Transport{
		TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
	}

	proxy.ModifyResponse = func(res *http.Response) error {
		// 复制响应Body
		bodyBytes, err := io.ReadAll(res.Body)
		if err != nil {
			log.Fatal(err)
		}
		res.Body.Close() // 关闭原始响应Body

		// 打印响应Body
		// log.Printf("<- Response Body: %s\n", string(bodyBytes))

		// 将响应Body写回去
		res.Body = io.NopCloser(bytes.NewBuffer(bodyBytes))
		return nil
	}

	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		log.Println("Received an HTTP request")

		// 复制请求Body
		bodyBytes, err := io.ReadAll(r.Body)
		if err != nil {
			log.Fatal(err)
		}
		r.Body.Close() // 关闭原始请求Body

		// 控制允许的方法
		if _, allow := allowMethods[r.Method]; !allow {
			w.WriteHeader(403)
			return
		}

		if "OPTIONS" == r.Method {
			// 解决跨域问题
			w.Header().Add("Access-Control-Allow-Origin", "*")
			w.Header().Add("Access-Control-Allow-Methods", "OPTIONS,HEAD,GET,POST")
			w.Header().Add("Access-Control-Allow-Headers", "*")
			w.Header().Add("Access-Control-Max-Age", "36000")

			w.WriteHeader(200)
			return
		}

		// 打印请求信息
		log.Printf("-> RemoteAddr: %s, Method: %s, URL: %s, Request Body: %s\n", r.RemoteAddr, r.Method, r.URL.String(), string(bodyBytes))

		// 将请求Body写回去
		r.Body = io.NopCloser(bytes.NewBuffer(bodyBytes))

		// 将请求转发到目标服务器
		proxy.ServeHTTP(w, r)
	})

	log.Fatal(http.ListenAndServe(":30080", nil))
}
