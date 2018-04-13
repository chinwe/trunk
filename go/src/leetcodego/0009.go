/*
9. 回文数

判断一个整数是否是回文数。不能使用辅助空间。

*/
package main

import (
	"fmt"
)

func isPalindrome(x int) bool {
	if x < 0 || x != 0 && x%10 == 0 {
		return false
	}
	sum := 0
	for x > sum {
		sum = sum*10 + x%10
		x /= 10
	}
	return x == sum || x == sum/10
}

func main() {
	fmt.Println(isPalindrome(123))
	fmt.Println(isPalindrome(1221))
	fmt.Println(isPalindrome(121))
}
