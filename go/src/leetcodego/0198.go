/*
198. 打家劫舍

你是一个专业的强盗，计划抢劫沿街的房屋。
每间房都藏有一定的现金，阻止你抢劫他们的唯一的制约因素就是相邻的房屋有保安系统连接，如果两间相邻的房屋在同一晚上被闯入，它会自动联系警方。

给定一个代表每个房屋的金额的非负整数列表，确定你可以在没有提醒警方的情况下抢劫的最高金额。
*/
package main

import (
	"fmt"
)

func max(x int, y int) int {
	if x > y {
		return x
	}
	return y
}

func rob(nums []int) int {
	size := len(nums)
	if 0 == size {
		return 0
	}
	if 1 == size {
		return nums[0]
	}
	moneys := make([]int, size)
	moneys[0], moneys[1] = nums[0], max(nums[0], nums[1])
	for i := 2; i < size; i++ {
		moneys[i] = max(moneys[i-2]+nums[i], moneys[i-1])
	}
	return moneys[size-1]
}

func main() {
	fmt.Println(rob([]int{1, 1, 1}))
	fmt.Println(rob([]int{1, 3, 1, 3, 100}))
}
