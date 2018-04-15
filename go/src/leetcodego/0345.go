/*
345. 反转字符串中的元音字母

编写一个函数，以字符串作为输入，反转该字符串中的元音字母。

示例 1：
给定 s = "hello", 返回 "holle".

示例 2：
给定 s = "leetcode", 返回 "leotcede".

注意:
元音字母不包括 "y".

*/
package main

import (
	"fmt"
)

func isVowel(c byte) bool {
	return 'a' == c || 'e' == c || 'i' == c || 'o' == c || 'u' == c || 'A' == c || 'E' == c || 'I' == c || 'O' == c || 'U' == c
}

func reverseVowels(s string) string {
	idxs := []int{}
	for i := 0; i < len(s); i++ {
		if isVowel(s[i]) {
			idxs = append(idxs, i)
		}
	}
	rv := []byte(s)
	for i := 0; i < len(idxs)/2; i++ {
		rv[idxs[i]], rv[idxs[len(idxs)-i-1]] = rv[idxs[len(idxs)-i-1]], rv[idxs[i]]
	}
	return string(rv)
}

func main() {
	fmt.Println(reverseVowels("hello"))
	fmt.Println(reverseVowels("leetcode"))
}
