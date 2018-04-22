/*
118. 杨辉三角
给定一个非负整数 numRows，生成杨辉三角的前 numRows 行。
在杨辉三角中，每个数是它左上方和右上方的数的和。

示例:

输入: 5
输出:
[
     [1],
    [1,1],
   [1,2,1],
  [1,3,3,1],
 [1,4,6,4,1]
]

*/

package main

import (
	"fmt"
)

func generate(numRows int) [][]int {
	pt := make([][]int, numRows)
	for i := 0; i < numRows; i++ {
		pt[i] = make([]int, i+1)
		for j := 0; j <= i; j++ {
			if i > 0 && j > 0 && j <= i-1 {
				pt[i][j] = pt[i-1][j] + pt[i-1][j-1]
			} else {
				pt[i][j] = 1
			}
		}
	}

	return pt
}

func main() {
	fmt.Println(generate(5))
}
