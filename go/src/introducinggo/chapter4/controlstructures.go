package main

import (
	"fmt"
)

func main() {

	// The for Statement
	for i := 1; i <= 10; i++ {
		fmt.Println(i)
	}

	// The if Statement
	for i := 1; i <= 4; i++ {
		if i%2 == 0 {
			fmt.Println("even")
		} else {
			fmt.Println("odd")
		}
	}

	// The switch Statement
	for i := 0; i <= 6; i++ {

		switch i {
		case 0:
			fmt.Println("Zero")
		case 1:
			fmt.Println("One")
		case 2:
			fmt.Println("Two")
		case 3:
			fmt.Println("Three")
		case 4:
			fmt.Println("Four")
		case 5:
			fmt.Println("Five")
		default:
			fmt.Println("Unknown Number")
		}
	}
}
