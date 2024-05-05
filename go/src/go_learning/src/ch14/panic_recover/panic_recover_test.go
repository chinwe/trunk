package panic_recover

import (
	"errors"
	"fmt"
	"testing"
)

func TestPanic(t *testing.T) {
	defer func() {
		if err := recover(); err != nil {
			fmt.Println("recovered from ", err)
		}
	}()

	t.Log("Start")
	panic(errors.New("Something wrong!"))
}
