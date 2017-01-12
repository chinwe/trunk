package main

import (
	"fmt"
)

const (
	a = 1
	b = iota
	c = iota
)

func main() {
	i := 13
	p := &i
	fmt.Println(p)

	for i >= 0 {
		fmt.Println(i)
		i--
	}

	var arr [3]int
	for i, count := 0, len(arr); i < count; i++ {
		arr[i] = i + 1
	}

	for i, j := range arr {
		fmt.Println(i, j)
	}

	for i := 0; i < 3; i++ {
		v := i
		fmt.Println(&v)
	}
}
