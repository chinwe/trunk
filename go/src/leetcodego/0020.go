/*
20. 有效的括号

给定一个只包括 '('，')'，'{'，'}'，'['，']' 的字符串，判断字符串是否有效。

括号必须以正确的顺序关闭，"()" 和 "()[]{}" 是有效的但是 "(]" 和 "([)]" 不是。

*/
package main

import (
	"container/list"
	"fmt"
)

func isValid(s string) bool {
	l := list.New()
	hm := make(map[rune]rune)
	hm[')'] = '('
	hm[']'] = '['
	hm['}'] = '{'
	for _, c := range s {
		back := l.Back()
		l.PushBack(c)
		left, ok := hm[c]
		if ok && back != nil {
			if back.Value != left {
				return false
			} else {
				l.Remove(l.Back())
				l.Remove(back)
			}
		}
	}
	return 0 == l.Len()
}

func main() {
	fmt.Println(isValid("()"))
	fmt.Println(isValid("()[]{}"))
	fmt.Println(isValid("(]"))
	fmt.Println(isValid("([)]"))
}
