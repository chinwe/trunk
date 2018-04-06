/*
217. 存在重复

给定一个整数数组，判断是否存在重复元素。
如果任何值在数组中出现至少两次，函数应该返回 true。如果每个元素都不相同，则返回 false。
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

func main() {
	nums := []int{1, 0, 1, 1}
	fmt.Println(containsDuplicate(nums))
}
