package main

import (
	"fmt"
)

func main() {

	// Arrays
	var arr [5]int
	arr[4] = 100
	fmt.Println(arr)

	x := [5]int{1, 2, 3, 4, 5}
	for i, v := range x {
		fmt.Println("[", i, "] = ", v)
	}

	// Slices
	// var s []int
	s0 := make([]int, 5)
	fmt.Println(s0)

	y := x[0:2]
	fmt.Println(y)

	s1 := append(y, 3, 4)
	fmt.Println(s1)

	s2 := []int{1, 2, 3}
	s3 := make([]int, 2)
	copy(s3, s2)
	fmt.Println(s3)

	arr1 := []int{48, 96, 86, 68,
		57, 82, 63, 70,
		37, 34, 83, 27,
		19, 97, 9, 17,
	}

	min := arr1[0]
	for i := 0; i < len(arr1); i++ {
		if arr1[i] < min {
			min = arr1[i]
		}
	}
	fmt.Println("min of arr1:", min)

	// Maps
	//var m1 map[string]int
	m1 := make(map[string]int)
	m1["Mon."] = 1
	m1["Tue."] = 2
	m1["Wed."] = 3
	fmt.Println(m1)

	delete(m1, "Wed.")

	for first, second := range m1 {
		fmt.Println(first, ":", second)
	}
}
