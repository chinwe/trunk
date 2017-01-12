package main

import (
	"fmt"
)

func main() {
	// array
	a := [10]int{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}
	s1 := a[:5]
	fmt.Println(s1)

	//slice
	var s2 []int
	s2 = []int{1, 2, 3, 4}
	fmt.Println(s2)

	//slice make
	s3 := make([]int, 0, 20)
	fmt.Println(len(s3), cap(s3))

	// copy
	s3 = append(s3, 5, 6, 7, 8, 9)
	fmt.Println(s3)
	copy(s2, s3)
	fmt.Println(s2)
	s4 := s3[:]
	fmt.Println(s4)
}
