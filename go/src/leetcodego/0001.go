/*
1. 两数之和

给定一个整数数列，找出其中和为特定值的那两个数。
你可以假设每个输入都只会有一种答案，同样的元素不能被重用。

示例:
给定 nums = [2, 7, 11, 15], target = 9
因为 nums[0] + nums[1] = 2 + 7 = 9
所以返回 [0, 1]
*/

package main

import (
	"fmt"
)

func twoSum(nums []int, target int) []int {
	hm := make(map[int]int)
	for i, v := range nums {
		if j, ok := hm[target-v]; ok {
			return []int{j, i}
		} else {
			hm[v] = i
		}
	}
	return nil
}

func main() {
	nums := []int{3, 2, 4}
	target := 6
	fmt.Println(twoSum(nums, target))
}
