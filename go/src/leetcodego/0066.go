/*
66. 加一

给定一个非负整数组成的非空数组，在该数的基础上加一，返回一个新的数组。

最高位数字存放在数组的首位， 数组中每个元素只存储一个数字。

你可以假设除了整数 0 之外，这个整数不会以零开头。

示例 1:

输入: [1,2,3]
输出: [1,2,4]
解释: 输入数组表示数字 123。
示例 2:

输入: [4,3,2,1]
输出: [4,3,2,2]
解释: 输入数组表示数字 4321。
*/
package main

import (
	"fmt"
)

func plusOne(digits []int) []int {
	bOF := true
	for i := len(digits) - 1; i >= 0; i-- {
		if bOF {
			digits[i]++
			bOF = false
		}
		if digits[i] >= 10 {
			digits[i] = 0
			bOF = true
		}
	}

	if bOF {
		newDigits := make([]int, len(digits)+1)
		newDigits[0] = 1
		return newDigits
	}

	return digits
}

func main() {
	fmt.Println(plusOne([]int{4, 3, 2, 1}))
	fmt.Println(plusOne([]int{1, 2, 3}))
	fmt.Println(plusOne([]int{}))
	fmt.Println(plusOne([]int{9}))
	fmt.Println(plusOne([]int{9, 9, 9}))
}
