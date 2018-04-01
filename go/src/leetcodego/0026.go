/*
26. 从排序数组中删除重复项

给定一个有序数组，你需要原地删除其中的重复内容，使每个元素只出现一次,并返回新的长度。
不要另外定义一个数组，您必须通过用 O(1) 额外内存原地修改输入的数组来做到这一点。

示例：
给定数组: nums = [1,1,2],
你的函数应该返回新长度 2, 并且原数组nums的前两个元素必须是1和2
不需要理会新的数组长度后面的元素
*/

package main

import (
	"fmt"
)

func removeDuplicates(nums []int) int {
	idx, size := 0, len(nums)
	for i := 1; i < size; i++ {
		if nums[i] != nums[i-1] {
			idx++
			nums[idx] = nums[i]
		}
	}
	return idx + 1
}

func main() {
	nums := []int{1, 2, 2, 3, 3, 3}
	fmt.Println(removeDuplicates(nums))
	fmt.Println(nums)
}
