/*
344. 反转字符串

请编写一个函数，其功能是将输入的字符串反转过来。

示例：

输入：s = "hello"
返回："olleh"

*/

package main

import (
	"fmt"
)

func reverseString(s string) string {
	rs := []rune(s)
	for i := 0; i < len(s)/2; i++ {
		rs[i], rs[len(rs)-i-1] = rs[len(rs)-i-1], rs[i]
	}
	return string(rs)
}

func main() {
	fmt.Println(reverseString("hello"))
}
