/*402. 移掉K位数字

给定一个以字符串表示的非负整数 num，移除这个数中的 k 位数字，使得剩下的数字最小。

注意:
num 的长度小于 10002 且 ≥ k。
num 不会包含任何前导零。

示例 1 :
输入: num = "1432219", k = 3
输出: "1219"
解释: 移除掉三个数字 4, 3, 和 2 形成一个新的最小的数字 1219。

示例 2 :
输入: num = "10200", k = 1
输出: "200"
解释: 移掉首位的 1 剩下的数字为 200. 注意输出不能有任何前导零。

示例 3 :
输入: num = "10", k = 2
输出: "0"
解释: 从原数字移除所有的数字，剩余为空就是0。
*/

package main

import (
	"fmt"
)

func removeKdigits(num string, k int) string {
	n := len(num)
	stack := make([]byte, n)
	count := 0
	// remove the digit that larger than digit after it
	for i := 0; i < n; i++ {
		for count != 0 && k > 0 && num[i] < stack[count-1] {
			count--
			k--
		}
		stack[count] = num[i]
		count++
	}

	// remove 0 at the beginning
	start := 0
	for start < count && stack[start] == '0' {
		start++
	}

	if start >= count-k {
		return "0"
	}

	// remove from lsb
	return string(stack[start : count-start-k])
}

func main() {
	fmt.Println(removeKdigits("1432219", 3))
	fmt.Println(removeKdigits("10200", 1))
	fmt.Println(removeKdigits("20", 2))
	fmt.Println(removeKdigits("10", 1))
	fmt.Println(removeKdigits("112", 1))
	fmt.Println(removeKdigits("1234567890", 9))
}
