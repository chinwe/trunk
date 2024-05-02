package func_test

import (
	"fmt"
	"math/rand"
	"testing"
	"time"
)

func returnMultiValues() (int, int) {
	return rand.Intn(10), rand.Intn(20)
}

func timeCostWrap(inner func(op int) int) func(op int) int {
	return func(n int) int {
		start := time.Now()
		ret := inner(n)
		fmt.Println("cost :", time.Since(start).Milliseconds())
		return ret
	}
}

func slowFun(op int) int {
	time.Sleep(time.Second * 1)
	return op
}

func TestFunc(t *testing.T) {
	i, j := returnMultiValues()
	t.Log(i, j)

	f := timeCostWrap(slowFun)
	t.Log(f(2))
}

func Sum(ops ...int) int {
	sum := 0
	for _, op := range ops {
		sum = sum + op
	}
	return sum
}

func TestSum(t *testing.T) {
	t.Log(Sum(1, 2, 3, 4, 5))
}

func TestDefer(t *testing.T) {
	defer func() {
		t.Log("Clean resources")
	}()

	t.Log("Start")
	// panic("err")
}
