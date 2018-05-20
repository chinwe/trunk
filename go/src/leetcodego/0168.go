/*
168. Excel表列名称
给定一个正整数，返回它在 Excel 表中相对应的列名称。
例如，

    1 -> A
    2 -> B
    3 -> C
    ...
    26 -> Z
    27 -> AA
    28 -> AB
	...

示例 1:
输入: 1
输出: "A"

示例 2:
输入: 28
输出: "AB"

示例 3:
输入: 701
输出: "ZY"

*/
package main

import (
	"fmt"
)

func reverseString(s string) string {
	runes := []rune(s)
	for from, to := 0, len(runes)-1; from < to; from, to = from+1, to-1 {
		runes[from], runes[to] = runes[to], runes[from]
	}
	return string(runes)
}

func convertToTitle(n int) string {
	title, table := []byte{}, "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
	for n > 0 {
		title = append(title, table[(n-1)%26])
		n = (n - 1) / 26
	}

	return reverseString(string(title))
}

func main() {
	fmt.Println(convertToTitle(1))
	fmt.Println(convertToTitle(28))
	fmt.Println(convertToTitle(701))
}
