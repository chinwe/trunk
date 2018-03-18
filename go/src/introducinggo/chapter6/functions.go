package main

import (
	"fmt"
)

func avg(x []int) int {
	defer func() {
		str := recover()
		fmt.Println(str)
	}()
	panic("Not Implemented")
}

func swap(x *int, y *int) {
	*x, *y = *y, *x
}

func add(arg ...int) int {
	total := 0
	for _, v := range arg {
		total += v
	}
	return total
}

func closure() {
	x := 0
	increment := func() int {
		x++
		return x
	}
	fmt.Println(increment())
	fmt.Println(increment())
}

func first() {
	fmt.Println("1st")
}
func second() {
	fmt.Println("2nd")
}

func main() {
	s1 := []int{0}
	fmt.Println(s1)

	// def panic recover
	a := avg(s1)
	fmt.Println(a)

	// Pointer new
	x := 1
	y := new(int)
	*y = 2
	swap(&x, y)
	fmt.Println(x, *y)

	fmt.Println(add(1, 2, 3))

	// closure
	closure()

	// def
	defer second()
	first()
}
