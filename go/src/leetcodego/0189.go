/*
189. 旋转数组

将包含 n 个元素的数组向右旋转 k 步。
例如，如果 n = 7, k = 3，给定数组  [1,2,3,4,5,6,7]，向右旋转后的结果为 [5,6,7,1,2,3,4]。

注意:
尽可能找到更多的解决方案，这里最少有三种不同的方法解决这个问题。

提示:
要求空间复杂度为 O(1)
*/
package main

import (
	"fmt"
)

func reverse(nums []int, start, end int) {
	for start < end {
		nums[start], nums[end] = nums[end], nums[start]
		start++
		end--
	}
}
func rotate(nums []int, k int) {
	n := len(nums)
	k = k % n
	if k == 0 {
		return
	}
	reverse(nums, 0, n-k-1)
	reverse(nums, n-k, n-1)
	reverse(nums, 0, n-1)
}

func main() {
	nums := []int{1, 2, 3, 4, 5, 6, 7}
	rotate(nums, 3)
	fmt.Println(nums)
}
