package fib_test

import (
	"testing"
)

func fib(n int) int {
	a, b := 1, 1
	for i := 0; i < n; i++ {
		tmp := a
		a = b
		b = tmp + a
	}
	return b
}

func TestFib(t *testing.T) {
	t.Log(fib(5))
}

func TestExchange(t *testing.T) {
	a, b := 1, 2
	a, b = b, a
	t.Log(a, b)
}
