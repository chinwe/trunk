package main

import "fmt"

var ch chan bool

func main() {
	ch = make(chan bool)

	go Go()

	<-ch
}

func Go() {
	fmt.Println("Go Go Go!!")

	ch <- true
}
