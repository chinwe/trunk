package utilalldone

import (
	"fmt"
	"runtime"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
)

func runTask(id int) string {
	time.Sleep(10 * time.Millisecond)
	return fmt.Sprintf("The result is from %d", id)
}

func AllResponse() string {
	numOfRunner := 10
	ch := make(chan string, numOfRunner)
	for i := 0; i < numOfRunner; i++ {
		go func(i int) {
			ret := runTask(i)
			ch <- ret
		}(i)
	}

	result := ""
	for i := 0; i < numOfRunner; i++ {
		result += <-ch + "\n"
	}
	return result
}

func TestFirstResponse(t *testing.T) {
	t.Log(runtime.NumGoroutine())
	t.Log(AllResponse())
	time.Sleep(1 * time.Second)
	t.Log(runtime.NumGoroutine())
}

func TestSomething(t *testing.T) {
	var a string = "Hello"
	var b string = "Hello"

	assert.Equal(t, a, b, "The two words should be the same.")
}
