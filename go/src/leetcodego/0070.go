/*
70. 爬楼梯

你正在爬楼梯。需要 n 步你才能到达顶部。
每次你可以爬 1 或 2 个台阶。你有多少种不同的方式可以爬到楼顶呢？
注意：给定 n 将是一个正整数。

示例 1：
输入： 2
输出： 2
说明： 有两种方法可以爬到顶端。
1.  1 步 + 1 步
2.  2 步


示例 2：
输入： 3
输出： 3
说明： 有三种方法可以爬到顶端。
1.  1 步 + 1 步 + 1 步
2.  1 步 + 2 步
3.  2 步 + 1 步
*/
package main

import (
	"fmt"
)

func climbStairs(n int) int {
	if n < 2 {
		return 1
	}
	dp := make([]int, n+1)
	dp[1], dp[2] = 1, 2
	for i := 3; i <= n; i++ {
		dp[i] = dp[i-1] + dp[i-2]
	}
	return dp[n]
}

func main() {
	fmt.Println(climbStairs(2))
	fmt.Println(climbStairs(3))
	fmt.Println(climbStairs(10))
}
