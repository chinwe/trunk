package cancel

import (
	"context"
	"fmt"
	"testing"
	"time"
)

func isCancelled(ctx context.Context) bool {
	select {
	case <-ctx.Done():
		return true
	default:
		return false
	}
}

func TestChannel(t *testing.T) {
	ctx, cancel := context.WithCancel(context.Background())
	for i := 0; i < 5; i++ {
		go func(i int, context context.Context) {
			for {
				if isCancelled(context) {
					break
				}
				time.Sleep(time.Millisecond * 5)
			}
			fmt.Println(i, "Cancelled")
		}(i, ctx)
	}
	cancel()
	time.Sleep(time.Second * 1)
}
