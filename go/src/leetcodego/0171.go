/*
171. Excel表列序号

给定一个Excel表格中的列名称，返回其相应的列序号。
例如，
    A -> 1
    B -> 2
    C -> 3
    ...
    Z -> 26
    AA -> 27
    AB -> 28
    ...

示例 1:
输入: "A"
输出: 1

示例 2:
输入: "AB"
输出: 28

示例 3:
输入: "ZY"
输出: 701
*/
package main

import (
	"fmt"
)

func titleToNumber(s string) int {
	n, base := 0, 1
	for i := len(s) - 1; i >= 0; i-- {
		n = n + (int(s[i]-'A')+1)*base
		base = 26 * base
	}

	return n
}

func main() {
	fmt.Println(titleToNumber("A"))
	fmt.Println(titleToNumber("AB"))
	fmt.Println(titleToNumber("ZY"))
}
