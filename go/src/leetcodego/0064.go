/*
64. 最小路径和

给定一个包含非负整数的 m x n 网格，请找出一条从左上角到右下角的路径，使得路径上的数字总和为最小。
说明：每次只能向下或者向右移动一步。

示例:
输入:
[
  [1,3,1],
  [1,5,1],
  [4,2,1]
]
输出: 7
解释: 因为路径 1→3→1→1→1 的总和最小。
*/
package main

import (
	"fmt"
)

func min(x int, y int) int {
	if x > y {
		return y
	}
	return x
}

func minPathSum(grid [][]int) int {
	m := len(grid)
	if 0 == m {
		return 0
	}
	n := len(grid[0])
	if 0 == n {
		return 0
	}

	for i := 0; i < m; i++ {
		for j := 0; j < n; j++ {
			if i > 0 && j > 0 {
				// from top or left
				grid[i][j] += min(grid[i-1][j], grid[i][j-1])
			} else if i > 0 {
				// first column
				grid[i][j] += grid[i-1][j]
			} else if j > 0 {
				// first row
				grid[i][j] += grid[i][j-1]
			}
		}
	}

	return grid[m-1][n-1]
}

func main() {
	grid := [][]int{{1, 3, 1}, {1, 5, 1}, {4, 2, 1}}
	fmt.Println(minPathSum(grid))
	fmt.Println(grid)
}
