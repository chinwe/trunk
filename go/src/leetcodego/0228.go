/*
228. 汇总区间
给定一个无重复元素的有序整数数组，返回数组中区间范围的汇总。

示例 1:

输入: [0,1,2,4,5,7]
输出: ["0->2","4->5","7"]
示例 2:

输入: [0,2,3,4,6,8,9]
输出: ["0","2->4","6","8->9"]
*/

package main

import (
	"fmt"
	"strconv"
)

func summaryRanges(nums []int) []string {
	r := []string{}
	size := len(nums)
	if size == 0 {
		return r
	}
	r = append(r, strconv.Itoa(nums[0]))
	rc := 0 // 连续计数
	for i := 1; i < len(nums); i++ {
		delta := nums[i] - nums[i-1]
		if delta != 1 {
			if rc > 0 {
				// 上一个区间结尾
				r[len(r)-1] = r[len(r)-1] + "->" + strconv.Itoa(nums[i-1])
			}

			// 新的区间开始
			rc = 0
			r = append(r, strconv.Itoa(nums[i]))
		} else {
			rc++
			if i == len(nums)-1 {
				// 最后一个数字是区间结尾
				r[len(r)-1] = r[len(r)-1] + "->" + strconv.Itoa(nums[i])
			}
		}
	}
	return r
}

func main() {
	nums1 := []int{0, 1, 2, 4, 5, 7}
	fmt.Println(summaryRanges(nums1))
	nums2 := []int{0, 2, 3, 4, 6, 8, 9}
	fmt.Println(summaryRanges(nums2))
}
