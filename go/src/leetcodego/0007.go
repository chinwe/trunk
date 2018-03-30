/*
7. 颠倒整数

给定一个范围为 32 位 int 的整数，将其颠倒。

例 1:
输入: 123
输出:  321

例 2:
输入: -123
输出: -321

例 3:
输入: 120
输出: 21

注意:
假设我们的环境只能处理 32 位 int 范围内的整数。根据这个假设，如果颠倒后的结果超过这个范围，则返回 0。
*/
package main

import (
	"fmt"
	"math"
)

func reverse(x int) int {
	irev := 0
	for {
		irev = irev*10 + x%10
		if irev > math.MaxInt32 || irev < math.MinInt32 {
			return 0
		}
		if x/10 != 0 {
			x = x / 10
		} else {
			break
		}
	}
	return irev
}

func main() {

	fmt.Println(reverse(123))
	fmt.Println(reverse(-123))
	fmt.Println(reverse(120))
	fmt.Println(reverse(1534236469))
}
