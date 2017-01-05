package main

import (
	"fmt"
	"time"
)

func main() {
	start := time.Now()
	ch := make(chan string)
	ticket := make(chan int)

	count := 10
	for i := 0; i < count; i++ {
		go gort(ch, ticket)
	}

	for i := 0; i < count; i++ {
		fmt.Println(<-ch)
	}

	secs := time.Since(start).Seconds()
	fmt.Printf("%.2fs", secs)
}

func gort(ch chan<- string, ticket chan<- int) {
	start := time.Now()

	secs := time.Since(start).Seconds()
	ch <- fmt.Sprintf("%.2fs %v", secs, ch)
}
