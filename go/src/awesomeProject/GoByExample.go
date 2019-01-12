package main

import (
	"fmt"
	"time"
)

const PI float32 = 3.1415926

func vals() (int, int) {
	return 0, 0
}

func intSeq() func() int {
	i := 0
	return func() int {
		i += 1
		return i
	}
}

func main() {
	name := "zhangsan"
	fmt.Println(name)

	for i := 0; i < 10; i++ {
		// fmt.Println(i)
	}

	switch time.Now().Weekday() {
	case time.Saturday, time.Sunday:
		fmt.Println("it's the weekend")
	default:
		fmt.Println("it's a weekday")
	}

	s := make([]string, 3)
	s[0] = "a"
	s[1] = "b"
	s[2] = "c"
	fmt.Println(s)

	fmt.Println(s[len(s)-1:])

	kvs := map[string]string{"a": "apple", "b": "banana"}
	for k, v := range kvs {
		fmt.Printf("%s -> %s\n", k, v)
	}

	_, c := vals()
	fmt.Println(c)

	nextInt := intSeq()

	fmt.Println(nextInt())
	fmt.Println(nextInt())
}
