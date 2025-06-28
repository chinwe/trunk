package groutine

import (
	"fmt"
	"sync"
	"testing"
)

func TestGroutine(t *testing.T) {
	var wg sync.WaitGroup
	for i := range 10 {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			fmt.Println(i)
		}(i)
	}
	wg.Wait()
}
