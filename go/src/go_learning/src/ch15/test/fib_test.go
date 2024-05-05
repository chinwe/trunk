package test

import (
	"go_learning/src/ch15/fib"
	"testing"
)

func TestFib(t *testing.T) {
	t.Log(fib.FibSeries(5))
}
