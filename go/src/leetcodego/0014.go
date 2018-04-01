/*
14. 最长公共前缀

编写一个函数来查找字符串数组中最长的公共前缀字符串。

*/

package main

import "fmt"

func longestCommonPrefix(strs []string) string {
	if len(strs) <= 0 {
		return ""
	}

	minLen := len(strs[0])
	for i := 1; i < len(strs); i++ {
		if len(strs[i]) < minLen {
			minLen = len(strs[i])
		}
	}

	commonPrefix := ""
	for i := 0; i < minLen; i++ {
		cur := strs[0][i : i+1]
		for j := 1; j < len(strs); j++ {
			if strs[j][i:i+1] != cur {
				return commonPrefix
			}
		}
		commonPrefix += cur
	}
	return commonPrefix
}

func main() {
	fmt.Println(longestCommonPrefix([]string{"abc", "a"}))
}
