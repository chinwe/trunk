package sharemem

import (
	"sync"
	"testing"
	"time"
)

func TestCounter(t *testing.T) {
	counter := 0
	for i := 0; i < 5000; i++ {
		go func() {
			counter++
		}()
	}
	time.Sleep(1 * time.Second)
	t.Logf("Counter = %d", counter)
}

func TestCounterThreadSafe(t *testing.T) {
	var mutex sync.Mutex
	var wg sync.WaitGroup
	counter := 0
	for i := 0; i < 5000; i++ {
		wg.Add(1)
		go func() {
			defer func() {
				mutex.Unlock()
			}()

			mutex.Lock()
			counter++
			wg.Done()
		}()
	}
	wg.Wait()
	t.Logf("Counter = %d", counter)
}
