/*
219. 存在重复 II

给定一个整数数组和一个整数 k，判断数组中是否存在两个不同的索引 i 和 j，使 nums [i] = nums [j]，并且 i 和 j 的绝对差值最大为 k。
*/

package main

import (
	"fmt"
)

func containsDuplicate(nums []int) bool {
	hm := make(map[int]int)
	for i, v := range nums {
		if _, ok := hm[v]; ok {
			return true
		}
		hm[v] = i
	}
	return false
}

func containsNearbyDuplicate(nums []int, k int) bool {
	hm := make(map[int]int)
	for i, v := range nums {
		if j, ok := hm[v]; ok && (i-j) <= k {
			return true
		}
		hm[v] = i
	}
	return false
}

func main() {
	nums := []int{1, 0, 1, 1}
	k := 1
	fmt.Println(containsNearbyDuplicate(nums, k))
}
