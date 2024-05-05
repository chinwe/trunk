package fib

import "fmt"

func init() {
	fmt.Println("fib package init")
}

func FibSeries(n int) []int {
	fib := make([]int, n)
	a, b := 1, 1
	for i := 0; i < n; i++ {
		fib[i] = a
		a, b = b, b+a
	}
	return fib
}
