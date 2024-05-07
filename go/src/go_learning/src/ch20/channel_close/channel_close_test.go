package channelclose

import (
	"fmt"
	"math/rand"
	"sync"
	"testing"
	"time"
)

func dataProducer(ch chan int, wg *sync.WaitGroup) {
	go func() {
		for i := 0; i < 10; i++ {
			ch <- i
		}
		close(ch)
		wg.Done()
	}()
}

func dataConsumer(ch chan int, wg *sync.WaitGroup, id int) {
	go func() {
		for {
			if data, ok := <-ch; ok {
				fmt.Printf("[id=%v] consume %v\n", id, data)
				time.Sleep(time.Millisecond * time.Duration(rand.Intn(100)))
			} else {
				break
			}
		}
		wg.Done()
	}()
}

func TestData(t *testing.T) {
	var wg sync.WaitGroup
	ch := make(chan int, 1)

	wg.Add(1)
	dataProducer(ch, &wg)

	wg.Add(1)
	dataConsumer(ch, &wg, 1)

	wg.Add(1)
	dataConsumer(ch, &wg, 2)

	wg.Add(1)
	dataConsumer(ch, &wg, 3)

	wg.Wait()
}
