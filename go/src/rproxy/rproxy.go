package main

import (
	"bytes"
	"crypto/tls"
	"io"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"
	"strings"

	"gopkg.in/yaml.v3"
)

type ProxyConfiguration struct {
	Endpoints []EndpointConfiguration `yaml:"endpoints"`
}

type EndpointConfiguration struct {
	Name          string            `yaml:"name"`
	Url           string            `yaml:"url"`
	ListenAddress string            `yaml:"listenAddress"`
	AlowMethods   string            `yaml:"allowMethods"`
	Headers       map[string]string `yaml:"headers"`
}

func main() {
	yamlFile, err := os.ReadFile("config.yaml")
	if err != nil {
		log.Fatalf("无法读取 YAML 文件: %v", err)
	}

	var config ProxyConfiguration
	err = yaml.Unmarshal(yamlFile, &config)
	if err != nil {
		log.Fatalf("无法解析 YAML 文件: %v", err)
	}

	c := make(chan bool, len(config.Endpoints))

	for _, endpoint := range config.Endpoints {
		go func() {
			c <- newProxy(endpoint)
		}()
	}

	<-c
}

func newProxy(endpoint EndpointConfiguration) bool {
	targetURL, _ := url.Parse(endpoint.Url)
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

	server := &http.Server{
		Addr: endpoint.ListenAddress,
		Handler: http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			log.Printf("[%s]-> Received an HTTP request\n", endpoint.Name)

			// 复制请求Body
			bodyBytes, err := io.ReadAll(r.Body)
			if err != nil {
				log.Fatal(err)
			}
			r.Body.Close() // 关闭原始请求Body

			// 控制允许的方法
			if !strings.Contains(endpoint.AlowMethods, r.Method) {
				w.WriteHeader(403)
				return
			}

			if r.Method == "OPTIONS" {
				// 解决跨域问题
				w.Header().Add("Access-Control-Allow-Origin", "*")
				w.Header().Add("Access-Control-Allow-Methods", endpoint.AlowMethods)
				w.Header().Add("Access-Control-Allow-Headers", "*")
				w.Header().Add("Access-Control-Max-Age", "36000")

				w.WriteHeader(200)
				return
			}

			// 添加自定义Header
			for k, v := range endpoint.Headers {
				r.Header.Add(k, v)
			}

			// 打印请求信息
			log.Printf("[%s] -> RemoteAddr: %s, Method: %s, URL: %s, Request Body: %s\n", endpoint.Name, r.RemoteAddr, r.Method, r.URL.String(), string(bodyBytes))

			// 将请求Body写回去
			r.Body = io.NopCloser(bytes.NewBuffer(bodyBytes))

			// 将请求转发到目标服务器
			proxy.ServeHTTP(w, r)
		}),
	}

	log.Printf("[%s] %s\n Listening...", endpoint.Name, endpoint.ListenAddress)
	log.Fatal(server.ListenAndServe())

	return false
}
