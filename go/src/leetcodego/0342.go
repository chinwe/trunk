/*
342. 4的幂

给定一个整数 (32位有符整数型)，请写出一个函数来检验它是否是4的幂。

示例:
当 num = 16 时 ，返回 true 。 当 num = 5时，返回 false。

问题进阶：你能不使用循环/递归来解决这个问题吗？

*/
package main

import (
	"fmt"
)

func isPowerOfFour(num int) bool {
	return (num > 0) && (num&(num-1) == 0) && (num%3 == 1)
}

func main() {
	fmt.Println(isPowerOfFour(0))
	fmt.Println(isPowerOfFour(1))
	fmt.Println(isPowerOfFour(4))
	fmt.Println(isPowerOfFour(5))
	fmt.Println(isPowerOfFour(16))
}
