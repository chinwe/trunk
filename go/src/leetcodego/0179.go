/*
179. 最大数

给定一个非负整数的列表，重新排列它们的顺序把他们组成一个最大的整数。
例如，给定 [3, 30, 34, 5, 9],最大的组成数是 9534330.
注意: 结果可能非常大，所以您需要返回一个字符串而不是整数。

致谢:
特别感谢 @ts 添加这个问题并创建所有测试用例。
*/
package main

import (
	"fmt"
	"sort"
	"strconv"
)

type NumSlice []int

func (p NumSlice) Len() int { return len(p) }
func (p NumSlice) Less(i, j int) bool {
	return strconv.Itoa(p[i])+strconv.Itoa(p[j]) > strconv.Itoa(p[j])+strconv.Itoa(p[i])
}
func (p NumSlice) Swap(i, j int) { p[i], p[j] = p[j], p[i] }

func largestNumber(nums []int) string {
	sort.Sort(NumSlice(nums))
	ret := ""

	for i := 0; i < len(nums); i++ {
		if i == 0 && nums[i] == 0 {
			return "0"
		}
		ret = ret + strconv.Itoa(nums[i])
	}
	return ret
}

func main() {
	nums := []int{3, 30, 34, 5, 9}
	fmt.Println(largestNumber(nums))

	nums1 := []int{3624, 4215, 5580, 9184, 9885, 704, 2755, 4027, 1535}
	fmt.Println(largestNumber(nums1))
}
