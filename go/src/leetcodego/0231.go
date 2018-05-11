/*
231. 2的幂

给定一个整数，写一个函数来判断它是否是2的幂。

*/
package main

import (
	"fmt"
)

func isPowerOfTwo(n int) bool {
	return n > 0 && (0 == n&(n-1))
}

func main() {
	fmt.Println(isPowerOfTwo(0))
	fmt.Println(isPowerOfTwo(1))
	fmt.Println(isPowerOfTwo(4))
	fmt.Println(isPowerOfTwo(3))
}
