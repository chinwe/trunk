package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
)

type FormatConfig struct {
	maxChunkCount int
}

func createChunk(config FormatConfig) {
	data := "12345678"

	for i := 0; i < config.maxChunkCount; i++ {
		err := ioutil.WriteFile("
			chunk", []byte(data), 0644)

		if err != nil {
			fmt.Printf("[err] Failed to WriteFile %v\n", err)
		}
	}
}

func main() {
	var config FormatConfig
	filename := "format.json"

	bytes, err := ioutil.ReadFile(filename)
	if err == nil {
		// 读取配置文件
		json.Unmarshal(bytes, &config)

		// 创建文件块
		createChunk(config)
	} else {
		fmt.Println("[err] Faile to parse file.[" + filename + "]")
	}
}
