/*
67. 二进制求和

给定两个二进制字符串，返回他们的和（用二进制表示）。

案例：
a = "11"
b = "1"
返回 "100" 。
*/

package main

import (
	"fmt"
)

func addBinary(a string, b string) string {
	ret := ""
	bPlus := false
	delta := len(a) - len(b)
	// add zero
	if delta > 0 {
		for delta > 0 {
			b = "0" + b
			delta--
		}
	} else {
		for delta < 0 {
			a = "0" + a
			delta++
		}
	}
	for i := len(a) - 1; i >= 0; i-- {
		bit := 0
		if i < len(a) {
			bit = bit + int(a[i]-'0')
		}

		if i < len(b) {
			bit = bit + int(b[i]-'0')
		}

		if bPlus {
			bit = bit + 1
			bPlus = false
		}

		if bit > 1 {
			bPlus = true
		}

		bit = bit % 2
		if bit == 1 {
			ret = "1" + ret
		} else {
			ret = "0" + ret
		}
	}

	if bPlus {
		ret = "1" + ret
	}

	return ret
}

func main() {
	fmt.Println(addBinary("1111", "1111"))
}
