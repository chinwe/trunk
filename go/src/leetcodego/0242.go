/*
242. 有效的字母异位词

给定两个字符串 s 和 t ，编写一个函数来判断 t 是否是 s 的一个字母异位词。

例如，
s = "anagram"，t = "nagaram"，返回 true
s = "rat"，t = "car"，返回 false

注意:
假定字符串只包含小写字母。

提升难度:
输入的字符串包含 unicode 字符怎么办？你能能否调整你的解法来适应这种情况？
*/
package main

import (
	"fmt"
	"sort"
)

type RuneSlice []rune

func (p RuneSlice) Len() int           { return len(p) }
func (p RuneSlice) Less(i, j int) bool { return p[i] < p[j] }
func (p RuneSlice) Swap(i, j int)      { p[i], p[j] = p[j], p[i] }

func isAnagram(s string, t string) bool {
	if len(s) != len(t) {
		return false
	}
	rs := []rune(s)
	rt := []rune(t)
	sort.Sort(RuneSlice(rs))
	sort.Sort(RuneSlice(rt))

	for i := 0; i < len(rs); i++ {
		if rs[i] != rt[i] {
			return false
		}
	}
	return true
}

func main() {
	fmt.Println(isAnagram("anagram", "nagaram"))
	fmt.Println(isAnagram("rat", "cat"))
	fmt.Println(isAnagram("你好", "好你"))
}
