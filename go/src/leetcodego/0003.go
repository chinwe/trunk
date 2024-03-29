/*

3. 无重复字符的最长子串

给定一个字符串，找出不含有重复字符的 最长子串 的长度。

示例：
给定 "abcabcbb" ，没有重复字符的最长子串是 "abc" ，那么长度就是3。
给定 "bbbbb" ，最长的子串就是 "b" ，长度是1。
给定 "pwwkew" ，最长子串是 "wke" ，长度是3。请注意答案必须是一个子串，"pwke" 是 子序列 而不是子串。

*/

package main

import (
	"fmt"
	"strings"
)

func lengthOfLongestSubstring(s string) int {
	var longlength int
	for i := len(s) - 1; i >= 0; i-- {
		sub := s[0:i]

		longlength = strings.Index(s, sub)

		fmt.Println(s, sub, longlength)

		if longlength > 0 {
			break
		}
	}
	return longlength
}

func main() {
	//fmt.Println(lengthOfLongestSubstring("abcabcbb"))
	//fmt.Println(lengthOfLongestSubstring("bbbbb"))
	fmt.Println(lengthOfLongestSubstring("pwwkew"))
}
