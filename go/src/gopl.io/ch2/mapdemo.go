package main

import (
	"fmt"
)

func main() {
	// swap key and value
	m1 := map[int]string{1: "a", 2: "b", 3: "c"}
	m2 := make(map[string]int)

	for k, v := range m1 {
		m2[v] = k
	}

	fmt.Println(m1)
	fmt.Println(m2)
}
