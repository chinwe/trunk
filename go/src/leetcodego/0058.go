/*
58. 最后一个单词的长度

给定一个字符串， 包含大小写字母、空格 ' '，请返回其最后一个单词的长度。
如果不存在最后一个单词，请返回 0 。

注意事项：一个单词的界定是，由字母组成，但不包含任何的空格。

案例:
输入: "Hello World"
输出: 5

*/

package main

import (
	"fmt"
	"strings"
)

func lengthOfLastWord(s string) int {
	sa := strings.Split(s, " ")
	for i := len(sa) - 1; i >= 0; i-- {
		if sa[i] != "" && sa[i] != " " {
			return len(sa[i])
		}
	}
	return 0
}

func main() {
	fmt.Println(lengthOfLastWord("hello world"))
	fmt.Println(lengthOfLastWord("hello"))
	fmt.Println(lengthOfLastWord("cab "))
	fmt.Println(lengthOfLastWord("a "))
	fmt.Println(lengthOfLastWord(" a"))
	fmt.Println(lengthOfLastWord("        "))
	fmt.Println(lengthOfLastWord("b   a    "))
}
