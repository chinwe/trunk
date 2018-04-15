/*
34. 搜索范围

给定一个按照升序排序的整数数组，和一个目标值。找出给定目标值的开始位置和结束位置。

你的算法时间复杂度必须是 O(log n) 级别。

如果数组中不存在目标值，返回 [-1, -1]。

例如:
给出 [5, 7, 7, 8, 8, 10] 和目标值 8，
返回 [3, 4]

*/

package main

import (
	"fmt"
)

func searchRange(nums []int, target int) []int {
	size := len(nums)
	ret := []int{-1, -1}
	if size == 0 {
		return ret
	}
	left, right := 0, size-1
	for left < right {
		middle := (left + right) / 2
		if nums[middle] < target {
			left = middle + 1
		} else {
			right = middle
		}
	}

	if nums[left] != target {
		return ret
	}

	ret[0] = left

	right = size - 1
	for left < right {
		middle := (left + right + 1) / 2
		if nums[middle] > target {
			right = middle - 1
		} else {
			left = middle
		}
	}

	ret[1] = left

	return ret
}

func main() {
	nums := []int{5, 7, 7, 8, 8, 10}
	fmt.Println(searchRange(nums, 8))
	fmt.Println(searchRange(nums, 4))
	fmt.Println(searchRange(nil, 4))
	fmt.Println(searchRange([]int{4}, 4))
	fmt.Println(searchRange([]int{4, 4}, 4))
}
