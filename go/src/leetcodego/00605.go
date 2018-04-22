/*
605. 种花问题

假设你有一个很长的花坛，一部分地块种植了花，另一部分却没有。可是，花卉不能种植在相邻的地块上，它们会争夺水源，两者都会死去。

给定一个花坛（表示为一个数组包含0和1，其中0表示没种植花，1表示种植了花），和一个数 n 。能否在不打破种植规则的情况下种入 n 朵花？能则返回True，不能则返回False。

示例 1:

输入: flowerbed = [1,0,0,0,1], n = 1
输出: True
示例 2:

输入: flowerbed = [1,0,0,0,1], n = 2
输出: False
注意:

数组内已种好的花不会违反种植规则。
输入的数组长度范围为 [1, 20000]。
n 是非负整数，且不会超过输入数组的大小。

*/
package main

import (
	"fmt"
)

func canPlaceFlowers(flowerbed []int, n int) bool {
	fcnt := 0
	for i := 0; i < len(flowerbed); i++ {
		prev, next := 0, 0
		if i > 0 {
			prev = flowerbed[i-1]
		}
		if i < len(flowerbed)-1 {
			next = flowerbed[i+1]
		}
		if prev != 1 && next != 1 && flowerbed[i] == 0 {
			fcnt++
			flowerbed[i] = 1
			i++
		} else if flowerbed[i] == 1 {
			i++
		} else if next == 1 {
			i = i + 2
		}
	}
	return fcnt >= n
}

func main() {
	fmt.Println(canPlaceFlowers([]int{1, 0, 0, 0, 1}, 1))
	fmt.Println(canPlaceFlowers([]int{1, 0, 0, 0, 0, 0, 1}, 2))
	fmt.Println(canPlaceFlowers([]int{0, 0, 1, 0, 1}, 1))
	fmt.Println(canPlaceFlowers([]int{1, 0, 1, 0, 0}, 1))

}
