package main

import (
	"fmt"
)

func main() {
	var x = "Hello, World"
	fmt.Println(x)

	var y int
	y = 256
	fmt.Println(y)

	pi := 3.1415926
	fmt.Println(pi)

	const name string = "go"
	fmt.Println(name)

	var (
		a = 1
		b = 2
		c = 3
	)
	fmt.Println(a + b + c)

	fmt.Print("Enter a number:")
	var in float64
	fmt.Scanf("%f", &in)
	out := in * 2
	fmt.Println(out)
}
