/*
829. Consecutive Numbers Sum

Given a positive integer N, how many ways can we write it as a sum of consecutive positive integers?

Example 1:
Input: 5
Output: 2
Explanation: 5 = 5 = 2 + 3

Example 2:
Input: 9
Output: 3
Explanation: 9 = 9 = 4 + 5 = 2 + 3 + 4

Example 3:
Input: 15
Output: 4
Explanation: 15 = 15 = 8 + 7 = 4 + 5 + 6 = 1 + 2 + 3 + 4 + 5

Note: 1 <= N <= 10 ^ 9.
*/
package main

import (
	"fmt"
)

func consecutiveNumbersSum(N int) int {
	l, r, sum, result := 0, 0, 0, 0
	for {
		for sum < N && r < N {
			r++
			sum += r
		}
		if sum < N {
			break
		} else if sum == N {
			result++
		}
		l++
		sum -= l
	}
	return result
}

func main() {
	//fmt.Println(consecutiveNumbersSum(5))
	//fmt.Println(consecutiveNumbersSum(9))
	fmt.Println(consecutiveNumbersSum(15))
	//fmt.Println(consecutiveNumbersSum(246854111))
}
