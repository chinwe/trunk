/*
53. 最大子序和

给定一个序列（至少含有 1 个数），从该序列中寻找一个连续的子序列，使得子序列的和最大。

例如，给定序列 [-2,1,-3,4,-1,2,1,-5,4]，
连续子序列 [4,-1,2,1] 的和最大，为 6。

扩展练习:
若你已实现复杂度为 O(n) 的解法，尝试使用更为精妙的分治法求解。

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

func maxSubArray(nums []int) int {
	maxSum, theSum := nums[0], nums[0]
	for i := 1; i < len(nums); i++ {
		theSum = max(theSum+nums[i], nums[i])
		if theSum > maxSum {
			maxSum = theSum
		}
	}
	return maxSum
}

func main() {
	fmt.Println(maxSubArray([]int{-2, 1, -3, 4, -1, 2, 1, -5, 4}))
}
