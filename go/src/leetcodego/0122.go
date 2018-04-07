/*
122. 买卖股票的最佳时机 II

假设有一个数组，它的第 i 个元素是一个给定的股票在第 i 天的价格。
设计一个算法来找到最大的利润。你可以完成尽可能多的交易（多次买卖股票）。然而，你不能同时参与多个交易（你必须在再次购买前出售股票）。
*/

package main

import (
	"fmt"
)

func maxProfit(prices []int) int {
	profit := 0
	for i := 1; i < len(prices); i++ {
		if (prices[i] - prices[i-1]) > 0 {
			profit += (prices[i] - prices[i-1])
		}
	}
	return profit
}

func main() {
	fmt.Println(maxProfit([]int{7, 1, 5, 3, 6, 4}))
	fmt.Println(maxProfit([]int{7, 6, 4, 3, 1}))
}
