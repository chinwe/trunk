package select_test

import (
	"fmt"
	"testing"
	"time"
)

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

func TestSelect(t *testing.T) {
	ch := asyncService()
	select {
	case <-ch:
		t.Log(ch)
	case <-time.After(time.Millisecond * 20):
		t.Error("time out")
	}
}
