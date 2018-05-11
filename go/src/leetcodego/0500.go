/*
500. 键盘行

给定一个单词列表，只返回可以使用在键盘同一行的字母打印出来的单词。键盘如下图所示。

QWERTYUIOP
ASDFGHJKL
ZXCVBNM

示例1:
输入: ["Hello", "Alaska", "Dad", "Peace"]
输出: ["Alaska", "Dad"]

注意:
你可以重复使用键盘上同一字符。
你可以假设输入的字符串将只包含字母。
*/

package main

import (
	"fmt"
	"strings"
)

func line(c rune) int {
	fr := "QWERTYUIOPqwertyuiop"
	sr := "ASDFGHJKLasdfghjkl"
	tr := "ZXCVBNMzxcvbnm"

	if strings.IndexRune(fr, c) >= 0 {
		return 1
	} else if strings.IndexRune(sr, c) >= 0 {
		return 2
	} else if strings.IndexRune(tr, c) >= 0 {
		return 3
	}
	return 0
}
func findWords(words []string) []string {
	ws := []string{}
	for _, word := range words {
		cur := -1
		for _, c := range word {
			if cur < 0 {
				cur = line(c)
			} else if cur != line(c) {
				cur = -1
				break
			}
		}
		if cur > 0 {
			ws = append(ws, word)
		}
	}
	return ws
}

func main() {
	fmt.Println(findWords([]string{"Hello", "Alaska", "Dad", "Peace"}))
}
