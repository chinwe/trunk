/*
35. 搜索插入位置

给定一个排序数组和一个目标值，如果在数组中找到目标值则返回索引。如果没有，返回到它将会被按顺序插入的位置。
你可以假设在数组中无重复元素。

案例 1:
输入: [1,3,5,6], 5
输出: 2

案例 2:
输入: [1,3,5,6], 2
输出: 1

案例 3:
输入: [1,3,5,6], 7
输出: 4

案例 4:
输入: [1,3,5,6], 0
输出: 0
*/

package main

import (
	"fmt"
)

func searchInsert(nums []int, target int) int {
	left, right := 0, len(nums)-1
	if target > nums[right] {
		return right + 1
	}
	for left < right {
		mid := left + (right-left)/2
		if nums[mid] >= target {
			right = mid
		} else {
			left = mid + 1
		}
	}
	return left
}

func main() {
	nums := []int{1, 3, 5, 6}
	fmt.Println(searchInsert(nums, 5))
	fmt.Println(searchInsert(nums, 2))
	fmt.Println(searchInsert(nums, 7))
	fmt.Println(searchInsert(nums, 1))
}
