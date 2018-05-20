/*
38. 报数
报数序列是指一个整数序列，按照其中的整数的顺序进行报数，得到下一个数。其前五项如下：
1.     1
2.     11
3.     21
4.     1211
5.     111221
6.	   312211
1 被读作  "one 1"  ("一个一") , 即 11。
11 被读作 "two 1s" ("两个一"）, 即 21。
21 被读作 "one 2",  "one 1" （"一个二" ,  "一个一") , 即 1211。

给定一个正整数 n ，输出报数序列的第 n 项。

注意：整数顺序将表示为一个字符串。

示例 1:
输入: 1
输出: "1"

示例 2:
输入: 4
输出: "1211"
*/
package main

import (
	"fmt"
	"strconv"
)

func countAndSay(n int) string {
	if 0 == n {
		return ""
	}
	if 1 == n {
		return "1"
	}

	say := "1"
	for n > 1 {
		ret, cnt := "", 1
		for i := 0; i < len(say); i++ {
			if i < len(say)-1 {
				if say[i+1] == say[i] {
					cnt++
					continue
				}
			}
			if cnt > 1 {
				ret += strconv.Itoa(cnt)
				ret += say[i-1 : i]
				cnt = 1
				i += cnt - 1

				continue
			}
			ret += "1"
			ret += say[i : i+1]
		}

		if cnt > 1 {
			ret += strconv.Itoa(cnt)
			ret += say[len(say)-1 : len(say)]
		}
		say = ret
		n--
	}
	return say
}

func main() {
	fmt.Println(countAndSay(1))
	fmt.Println(countAndSay(2))
	fmt.Println(countAndSay(3))
	fmt.Println(countAndSay(4))
	fmt.Println(countAndSay(5))
	fmt.Println(countAndSay(6))
}
