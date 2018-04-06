/*
412. Fizz Buzz

写一个程序，输出从 1 到 n 数字的字符串表示。
1. 如果 n 是3的倍数，输出“Fizz”；
2. 如果 n 是5的倍数，输出“Buzz”；
3.如果 n 同时是3和5的倍数，输出 “FizzBuzz”。

示例：
n = 15,
返回:
[
    "1",
    "2",
    "Fizz",
    "4",
    "Buzz",
    "Fizz",
    "7",
    "8",
    "Fizz",
    "Buzz",
    "11",
    "Fizz",
    "13",
    "14",
    "FizzBuzz"
]
*/

package main

import (
	"fmt"
	"strconv"
)

func fizzBuzz(n int) []string {
	strs := make([]string, n)
	for i := 1; i <= n; i++ {
		if i%15 == 0 {
			strs[i-1] = "FizzBuzz"
		} else if i%3 == 0 {
			strs[i-1] = "Fizz"
		} else if i%5 == 0 {
			strs[i-1] = "Buzz"
		} else {
			strs[i-1] = strconv.Itoa(i)
		}
	}
	return strs
}

func main() {
	fmt.Println(fizzBuzz(15))
}
