package main

import (
	"fmt"
)

func main() {

	f := closure(10)
	fmt.Println(f(1))
	fmt.Println(f(2))

	fmt.Println("a")
	defer fmt.Println("b")
	defer fmt.Println("c")
}

func aaaa(a ...int) {
	fmt.Println(a)
}

func closure(x int) func(int) int {
	return func(y int) int {
		return x + y
	}
}
