/*
240. 搜索二维矩阵 II

编写一个高效的算法来搜索 m x n 矩阵中的一个目标值。该矩阵具有以下特性：
每行的元素从左到右升序排列。
每列的元素从上到下升序排列。

例如，
考虑下面的矩阵：
[
  [1,   4,  7, 11, 15],
  [2,   5,  8, 12, 19],
  [3,   6,  9, 16, 22],
  [10, 13, 14, 17, 24],
  [18, 21, 23, 26, 30]
]
给定目标值 target = 5, 返回 true。

给定目标值 target = 20, 返回 false。

*/

package main

import (
	"fmt"
)

// 思路：从左下角或右上角开始搜索均可，每次判断大小换行或者换列移动。比较matrix[row][col]与target的关系。
func searchMatrix(matrix [][]int, target int) bool {
	m := len(matrix)
	if m == 0 {
		return false
	}
	n := len(matrix[0])
	if n == 0 {
		return false
	}
	row := m - 1
	col := 0
	for row >= 0 && col < n {
		if matrix[row][col] == target {
			return true
		} else if matrix[row][col] > target {
			row--
		} else {
			col++
		}
	}
	return false
}

func main() {
	matrix := [][]int{
		{1, 4, 7, 11, 15},
		{2, 5, 8, 12, 19},
		{3, 6, 9, 16, 22},
		{10, 13, 14, 17, 24},
		{18, 21, 23, 26, 30}}

	fmt.Println(searchMatrix(matrix, 5))
	fmt.Println(searchMatrix(matrix, 20))
	fmt.Println(searchMatrix(matrix, 15))
}
