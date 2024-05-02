package string_test

import (
	"strconv"
	"strings"
	"testing"
)

func TestStringFunc(t *testing.T) {
	s := "A,B,C"

	parts := strings.Split(s, ",")
	for _, part := range parts {
		t.Log(part)
	}

	s1 := strings.Join(parts, "-")
	t.Log(s1)
}

func TestStringConv(t *testing.T) {
	s := strconv.Itoa(10)
	t.Log("str " + s)
}
