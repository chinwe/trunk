package error_test

import (
	"errors"
	"testing"
)

func GetValue(n int) error {
	if n < 0 {
		return errors.New("n must greater then 0")
	}

	return nil
}

func TestGetValue(t *testing.T) {
	if err := GetValue(-1); err != nil {
		t.Error(err)
	} else {
		t.Log("ok")
	}
}
