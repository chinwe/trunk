package csp

import (
	"fmt"
	"testing"
	"time"
)

func otherTask() {
	fmt.Println("working on something else")
	time.Sleep(time.Millisecond * 100)
	fmt.Println("Task is done.")
}

func service() string {
	time.Sleep(time.Millisecond * 50)
	return "Done"
}

func asyncService() chan string {
	ch := make(chan string, 1)
	go func() {
		ret := service()
		fmt.Println("returned result.")
		ch <- ret
		fmt.Println("service exited.")
	}()
	return ch
}

func TestAsync(t *testing.T) {
	ch := asyncService()
	otherTask()
	fmt.Println(<-ch)
}
