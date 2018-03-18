package main

import "fmt"

func numbersType() {
	fmt.Println("1 + 1 =", 1+1)
	fmt.Println("1 / 3 =", 1.0/3)
	fmt.Println("3 % 2 =", 3%2)
}

func stringsType() {
	fmt.Println(len("Hello, World"))
	fmt.Println("Hello, World"[1])
	fmt.Println("Hello, " + "World")
}

func booleansType() {
	fmt.Println(true && false)
	fmt.Println(true || false)
	fmt.Println(!false)
}

func main() {
	numbersType()
	stringsType()
	booleansType()
}
